package com.example.xplorenow.bookings;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.BookingsAdapter;
import com.example.xplorenow.data.local.CachedBooking;
import com.example.xplorenow.data.local.CachedBookingDao;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyBookingsFragment extends Fragment {

    private static final String TAG = "MyBookingsFragment";

    @Inject ApiService apiService;
    @Inject CachedBookingDao cachedBookingDao;

    private BookingsAdapter adapter;
    private TextView tvOfflineMode;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);
        RecyclerView rvBookings = view.findViewById(R.id.rvBookings);
        tvOfflineMode = view.findViewById(R.id.tvOfflineMode);

        // Req. 21: banner empieza oculto
        tvOfflineMode.setVisibility(View.GONE);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new BookingsAdapter(new BookingsAdapter.OnBookingInteractionListener() {
            @Override
            public void onCancelClick(Booking booking) {
                showCancelDialog(view, booking, progressBar, tvError);
            }

            @Override
            public void onItemClick(Booking booking) {
                Bundle args = new Bundle();
                args.putInt("activityId", booking.getActivityId());
                Navigation.findNavController(view).navigate(
                        R.id.action_myBookings_to_activityDetail, args);
            }
        });

        rvBookings.setAdapter(adapter);

        // Req. 20: registrar callback de reconexión para auto-sync
        registerConnectivityCallback(progressBar, tvError);

        // Carga inicial
        loadBookings(progressBar, tvError);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Req. 18 + 21: cargar reservas, fallback a Room si no hay conexión
    // ─────────────────────────────────────────────────────────────────────────
    private void loadBookings(ProgressBar progressBar, TextView tvError) {
        // Capturar tvEmpty de forma final para que sea accesible dentro del callback
        View rootView = getView();
        final TextView tvEmpty = rootView != null ? rootView.findViewById(R.id.tvEmpty) : null;
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Booking>>> call,
                                   @NonNull Response<ApiResponse<List<Booking>>> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().getData();
                    if (bookings == null) bookings = new ArrayList<>();

                    // Req. 20: sincronizar Room con la respuesta fresca del servidor
                    syncCacheFromServer(bookings);

                    adapter.setBookings(bookings);
                    tvOfflineMode.setVisibility(View.GONE);  // online → ocultar banner

                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Booking>>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                Log.e(TAG, "onFailure - sin conexión, cargando desde Room: " + t.getMessage());

                // Req. 18 + 21: sin conexión → cargar desde Room y mostrar banner
                loadFromCache(tvError, tvEmpty);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Req. 18: cargar reservas confirmadas desde Room (en hilo secundario)
    // ─────────────────────────────────────────────────────────────────────────
    private void loadFromCache(TextView tvError, @Nullable TextView tvEmpty) {
        new Thread(() -> {
            List<CachedBooking> cached = cachedBookingDao.getAllBookings();
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                if (cached != null && !cached.isEmpty()) {
                    // Req. 21: mostrar aviso visual claro
                    tvOfflineMode.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);

                    // Convertir CachedBooking → Booking para reusar el adapter existente
                    List<Booking> offlineBookings = new ArrayList<>();
                    for (CachedBooking cb : cached) {
                        Booking b = new Booking();
                        b.setId(Integer.parseInt(cb.getId()));
                        b.setStatus(cb.getStatus());
                        b.setQuantity(cb.getQuantity());

                        // Inyectar datos en activity_detail para que el adapter los muestre
                        Activity act = new Activity();
                        act.setTitle(cb.getActivityTitle());
                        act.setMeetingPoint(cb.getMeetingPoint());
                        b.setActivityDetail(act);
                        // Bug fix: usar el activityId real, no el id de la reserva
                        b.setActivityId(cb.getActivityId());

                        offlineBookings.add(b);
                    }
                    adapter.setBookings(offlineBookings);
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(offlineBookings.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } else {
                    // Sin cache y sin internet
                    tvOfflineMode.setVisibility(View.VISIBLE);
                    tvError.setText(getString(R.string.error_connection));
                    tvError.setVisibility(View.VISIBLE);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Req. 20: sincronizar Room cuando el servidor responde correctamente
    //          (refleja cancelaciones y reprogramaciones del backend)
    // ─────────────────────────────────────────────────────────────────────────
    private void syncCacheFromServer(List<Booking> bookings) {
        new Thread(() -> {
            cachedBookingDao.clearAllBookings();   // limpiar datos viejos
            List<CachedBooking> toCache = new ArrayList<>();
            for (Booking b : bookings) {
                // Req. 20: cachear todas las reservas excepto las canceladas
                // (acepta mayúsculas/minúsculas por si el backend varía)
                String status = b.getStatus() != null ? b.getStatus().toUpperCase() : "";
                if ("CANCELED".equals(status) || "CANCELLED".equals(status)) continue;

                String imgUrl = "";
                if (b.getActivityDetail() != null
                        && b.getActivityDetail().getImages() != null
                        && !b.getActivityDetail().getImages().isEmpty()) {
                    imgUrl = b.getActivityDetail().getImages().get(0).getImageUrl();
                }
                toCache.add(new CachedBooking(
                        String.valueOf(b.getId()),
                        b.getActivityDetail() != null ? b.getActivityDetail().getTitle() : "",
                        b.getDate(),
                        b.getActivityDetail() != null ? b.getActivityDetail().getMeetingPoint() : "",
                        b.getStatus() != null ? b.getStatus() : "",
                        imgUrl,
                        "VOUCHER-" + b.getId(),
                        b.getQuantity(),
                        b.getActivityId()  // guardar activityId para navegar al detalle offline
                ));
            }
            // Guardar aunque esté vacío (para reflejar que no hay reservas activas)
            cachedBookingDao.insertBookings(toCache);
            Log.d(TAG, "syncCacheFromServer: " + toCache.size() + " reservas guardadas en Room");
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Req. 20: escuchar reconexión y auto-sincronizar
    // ─────────────────────────────────────────────────────────────────────────
    private void registerConnectivityCallback(ProgressBar progressBar, TextView tvError) {
        connectivityManager = (ConnectivityManager)
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    // Solo sincroniza si el banner estaba visible (es decir, estábamos offline)
                    if (tvOfflineMode.getVisibility() == View.VISIBLE) {
                        Log.d(TAG, "Conexión restaurada → sincronizando (req. 20)");
                        loadBookings(progressBar, tvError);
                    }
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar el callback para evitar memory leaks
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                Log.w(TAG, "NetworkCallback ya desregistrado: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancelar reserva (igual que antes)
    // ─────────────────────────────────────────────────────────────────────────
    private void showCancelDialog(View view, Booking booking, ProgressBar pb, TextView err) {
        String policy = getString(R.string.default_cancellation_policy);

        if (booking.getActivityDetail() != null &&
                booking.getActivityDetail().getCancellationPolicy() != null &&
                !booking.getActivityDetail().getCancellationPolicy().trim().isEmpty()) {
            policy = booking.getActivityDetail().getCancellationPolicy();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_cancel_booking)
                .setMessage(getString(R.string.msg_cancel_policy, policy))
                .setPositiveButton(R.string.action_confirm,
                        (d, w) -> performApiCancel(view, booking.getId(), pb, err))
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    private void performApiCancel(View view, int bookingId, ProgressBar progressBar, TextView tvError) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.cancelBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call,
                                   @NonNull Response<ApiResponse<Booking>> response) {

                if (response.isSuccessful()) {
                    // Req. 20: recargar desde el servidor al cancelar → sincroniza Room
                    loadBookings(progressBar, tvError);
                } else {
                    progressBar.setVisibility(View.GONE);
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
package com.example.xplorenow.bookings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.CheckInRequest;
import com.example.xplorenow.data.model.QrPayload;
import com.example.xplorenow.data.network.ApiService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class QrScannerFragment extends Fragment {

    private static final String TAG = "QrScannerFragment";

    @Inject
    ApiService apiService;

    private ExecutorService cameraExecutor;
    private boolean isScanning = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreviewView previewView = view.findViewById(R.id.previewView);
        LinearLayout layoutResult = view.findViewById(R.id.layoutResult);
        TextView tvResultMessage = view.findViewById(R.id.tvResultMessage);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        Button btnRetry = view.findViewById(R.id.btnRetry);

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnRetry.setOnClickListener(v -> {
            layoutResult.setVisibility(View.GONE);
            isScanning = true;
        });

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera(previewView, layoutResult, tvResultMessage, progressBar);
                    } else {
                        showError("Permiso de cámara requerido", layoutResult, tvResultMessage, progressBar);
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(previewView, layoutResult, tvResultMessage, progressBar);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera(PreviewView previewView, LinearLayout layoutResult, TextView tvResultMessage, ProgressBar progressBar) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                BarcodeScanner scanner = BarcodeScanning.getClient(
                        new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
                );

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> processImageProxy(scanner, imageProxy, layoutResult, tvResultMessage, progressBar));

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error iniciando cámara", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void processImageProxy(BarcodeScanner scanner, ImageProxy imageProxy, LinearLayout layoutResult, TextView tvResultMessage, ProgressBar progressBar) {
        if (imageProxy.getImage() != null && isScanning) {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            if (isScanning && barcode.getRawValue() != null) {
                                isScanning = false;
                                executeCheckIn(barcode.getRawValue(), layoutResult, tvResultMessage, progressBar);
                                break;
                            }
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void executeCheckIn(String jsonQr, LinearLayout layoutResult, TextView tvResultMessage, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);

        try {
            Gson gson = new Gson();
            QrPayload payload = gson.fromJson(jsonQr, QrPayload.class);

            if (payload == null || payload.getAvailabilityId() == null) {
                showError("QR inválido o irreconocible", layoutResult, tvResultMessage, progressBar);
                return;
            }

            apiService.checkInBooking(new CheckInRequest(payload.getAvailabilityId())).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        layoutResult.setVisibility(View.VISIBLE);
                        tvResultMessage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_green));
                        tvResultMessage.setText("Asistencia confirmada");

                        // Agregar el retraso y la navegación hacia atrás
                        if (getView() != null) {
                            getView().postDelayed(() -> {
                                if (isAdded()) {
                                    androidx.navigation.Navigation.findNavController(getView()).popBackStack();
                                }
                            }, 2000); // Retraso de 2 segundos
                        }

                    } else {
                        showError("Error: Reserva no válida o inexistente", layoutResult, tvResultMessage, progressBar);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    showError("Error de red: " + t.getMessage(), layoutResult, tvResultMessage, progressBar);
                }
            });

        } catch (JsonSyntaxException e) {
            showError("Formato de QR incorrecto", layoutResult, tvResultMessage, progressBar);
        }
    }

    private void showError(String message, LinearLayout layoutResult, TextView tvResultMessage, ProgressBar progressBar) {
        progressBar.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
        tvResultMessage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_red));
        tvResultMessage.setText(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
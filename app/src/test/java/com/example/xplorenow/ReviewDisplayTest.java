package com.example.xplorenow;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Verifica la lógica de decisión del HistoryAdapter para mostrar
 * "Calificar" o "Ver calificación" en cada ítem del historial (Req 13).
 *
 * Reproduce la condición de HistoryAdapter.onBindViewHolder():
 *   if (review != null && review.getId() > 0) → mostrar "Ver calificación"
 *   else → mostrar "Calificar"
 */
public class ReviewDisplayTest {

    // Objeto mínimo que replica Review para tests
    private static class FakeReview {
        final int id;
        final int activityRating;
        final int guideRating;
        final String comment;

        FakeReview(int id, int activityRating, int guideRating, String comment) {
            this.id = id;
            this.activityRating = activityRating;
            this.guideRating = guideRating;
            this.comment = comment;
        }
    }

    // --- Réplica de la lógica del adapter ---

    private boolean shouldShowVerCalificacion(FakeReview review) {
        return review != null && review.id > 0;
    }

    private boolean shouldShowCalificar(FakeReview review) {
        return !shouldShowVerCalificacion(review);
    }

    // --- Tests ---

    @Test
    public void reviewNull_debeCalificar() {
        assertTrue("Sin review debe mostrar botón Calificar", shouldShowCalificar(null));
        assertFalse("Sin review NO debe mostrar Ver calificación", shouldShowVerCalificacion(null));
    }

    @Test
    public void reviewConIdCero_debeCalificar() {
        // id=0 significa que la review está vacía/inválida
        FakeReview review = new FakeReview(0, 0, 0, null);
        assertTrue("Review con id=0 debe mostrar Calificar", shouldShowCalificar(review));
        assertFalse("Review con id=0 NO debe mostrar Ver calificación", shouldShowVerCalificacion(review));
    }

    @Test
    public void reviewConIdPositivo_debeVerCalificacion() {
        FakeReview review = new FakeReview(42, 4, 5, "Excelente actividad");
        assertTrue("Review con id>0 debe mostrar Ver calificación", shouldShowVerCalificacion(review));
        assertFalse("Review con id>0 NO debe mostrar Calificar", shouldShowCalificar(review));
    }

    @Test
    public void reviewConIdPositivoSinComentario_debeVerCalificacion() {
        // Comentario es opcional — no afecta si se muestra el botón
        FakeReview review = new FakeReview(7, 3, 4, "");
        assertTrue("Review sin comentario pero con id>0 debe mostrar Ver calificación",
                shouldShowVerCalificacion(review));
    }

    @Test
    public void reviewConIdNegativo_debeCalificar() {
        // ID negativo no es un estado válido — se trata como sin review
        FakeReview review = new FakeReview(-1, 0, 0, null);
        assertTrue("Review con id negativo debe mostrar Calificar", shouldShowCalificar(review));
    }
}

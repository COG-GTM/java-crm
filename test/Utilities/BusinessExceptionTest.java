package Utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BusinessException} (Group D — Utilities), covering all
 * three constructors.
 */
class BusinessExceptionTest {

    @Test
    void isACheckedException() {
        assertTrue(Exception.class.isAssignableFrom(BusinessException.class));
        assertTrue(new BusinessException() instanceof Exception);
    }

    @Test
    void noArgConstructorHasNoMessageOrCause() {
        BusinessException ex = new BusinessException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void causeConstructorWrapsGivenException() {
        Exception cause = new IllegalStateException("boom");
        BusinessException ex = new BusinessException(cause);
        assertSame(cause, ex.getCause());
        // super(Throwable) derives the message from the cause's toString().
        assertEquals(cause.toString(), ex.getMessage());
    }

    @Test
    void messageConstructorSetsMessageAndHasNoCause() {
        BusinessException ex = new BusinessException("something went wrong");
        assertEquals("something went wrong", ex.getMessage());
        assertNull(ex.getCause());
    }
}

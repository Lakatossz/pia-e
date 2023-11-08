package cz.zcu.kiv.mjakubas.piae.sem.core.service;

/**
 * Represents generic service error -> most (if not all) possible errors are handled in fronted, thus
 * user gets proper error if he uses the application as intended -> otherwise returns generic error.
 * If this error is thrown -> backend data check was successful in handling invalid data.
 */
public class ServiceException extends RuntimeException {
}

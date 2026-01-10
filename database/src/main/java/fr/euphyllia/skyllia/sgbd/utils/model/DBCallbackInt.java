package fr.euphyllia.skyllia.sgbd.utils.model;

/**
 * The {@code DBCallbackInt} interface defines a single method
 * to process an integer result, typically the number of affected rows
 * from a DML operation (INSERT, UPDATE, DELETE).
 */
public interface DBCallbackInt {

    /**
     * Processes the provided integer result.
     *
     * @param var1 the integer result to be processed, often the number of rows affected
     *             by an INSERT, UPDATE, or DELETE operation
     */
    void run(int var1);
}

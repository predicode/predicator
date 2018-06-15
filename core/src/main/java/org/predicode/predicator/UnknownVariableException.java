package org.predicode.predicator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;


/**
 * Exception thrown when attempt is made to access the variable not present in original query.
 * I.e. the variable without any {@link Knowns#resolution(Variable) resolution}.
 *
 * <p>This typically happens if the variable is not listed when initial {@link Knowns} constructed.</p>
 */
public class UnknownVariableException extends NoSuchElementException  {

    private static final long serialVersionUID = -4555753603447892736L;

    @Nonnull
    private final Variable variable;

    /**
     * Constructs exception instance with default message.
     *
     * @param variable unmapped variable.
     */
    public UnknownVariableException(@Nonnull Variable variable) {
        this(variable, null);
    }

    /**
     * Constructs exception instance.
     *
     * @param variable unmapped variable.
     * @param message optional message.
     */
    public UnknownVariableException(@Nonnull Variable variable, @Nullable String message) {
        super(message != null ? message : "Unknown variable " + variable);
        this.variable = variable;
    }

    @Nonnull
    public final Variable getVariable() {
        return this.variable;
    }

}

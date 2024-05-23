package com.kpi.tuke.scql;

/**
 * Interface used for definition of filterable classes.
 */
public interface Filterable {

    /**
     * Definition of the filter methods.
     * @param filters filters to apply.
     * @return filtered {@code Data} by applied filters.
     */
    Data[] filter(Filter[] filters);

}

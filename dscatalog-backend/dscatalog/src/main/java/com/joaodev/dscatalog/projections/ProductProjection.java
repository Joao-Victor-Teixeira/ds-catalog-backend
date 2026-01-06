package com.joaodev.dscatalog.projections;

public interface ProductProjection extends IdProjection<Long> {

    Long getId();
    String getName();
}

package com.ronilsonalves.gestaoporto.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public abstract class GenericEntity<T extends GenericEntity> {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GenericEntity)) {
            return false; // null or other class
        }
        GenericEntity<GenericEntity> other = (GenericEntity<GenericEntity>) obj;

        if (id != null) {
            return id.equals(other.id);
        }
        return super.equals(other);
    }
}

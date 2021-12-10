/*
 * Demo Dairy
 */

package dairypipel;

import java.util.Objects;

/** Replacement of Alert */
/** A simple cow recognition process event from the image stream. */
@SuppressWarnings("unused")
public final class RecResult {

    public long id;
    public Integer recResult = 1;
    public float bcsResult = 1.0f;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecResult() {
        return this.recResult;
    }

    public void setRecResult(Integer recResult){
        this.recResult = recResult;
    }

    public Float getBCSResult() {
        return this.bcsResult;
    }

    public void setBCSResult(Float bcsResult){
        this.bcsResult = bcsResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecResult recResult = (RecResult) o;
        return id == recResult.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RecResult{" + "id=" + id + ",bcs Result=" + Float.toString(bcsResult) + '}';
    }
}

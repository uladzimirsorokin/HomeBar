package sorokinuladzimir.com.homebarassistant.repository;

import java.util.List;

public interface Repository<T> {
    void add(T item);

    void add(Iterable<T> items);

    void update(T item);

    void remove(T item);

    void remove(Specification specification);

    List<T> query(Specification specification);

    List<T> getAll(Specification specification);
}

package sorokinuladzimir.com.homebarassistant.repository;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.entity.Drink;

public class RemoteDrinkRepository implements Repository<Drink> {

    @Override
    public void add(Drink item) {
        //add to db drink and ingredients
    }

    @Override
    public void add(Iterable<Drink> items) {

    }

    @Override
    public void update(Drink item) {

    }

    @Override
    public void remove(Drink item) {

    }

    @Override
    public void remove(Specification specification) {

    }

    @Override
    public List<Drink> query(Specification specification) {
        return null;
    }

    @Override
    public List<Drink> getAll(Specification specification) {
        return null;
    }
}

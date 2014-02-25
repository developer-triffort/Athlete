package com.athlete.services;
/**
 * @author edBaev
 * */
public interface IAppFilter<T> {
	boolean performFiltering(CharSequence constraint, T item);
}

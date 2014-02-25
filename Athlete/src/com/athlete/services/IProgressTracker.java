package com.athlete.services;
/**
 * @author edBaev
 * */
interface IProgressTracker {
	public void onProgress();
	public void onComplete(Object task);
}

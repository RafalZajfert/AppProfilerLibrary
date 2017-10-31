package sftware.rsquared.appprofiler;

/**
 * @author Rafał Zajfert
 */
public interface OnProfileChangedListener {

	/**
	 * Method called if app profile changed. This method is called in {@code AppProfiler.init()} (in this case {@code initialization} will be true) and on AppProfilerActivity finish
	 *
	 * @param fromInit true if called from {@code AppProfiler.init()} method, false otherwise
	 */
	void onProfileChanged(boolean fromInit);

}

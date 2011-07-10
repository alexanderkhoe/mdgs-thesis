package id.mdgs.master;

public interface IClassify<T,E> {
	public int classify(E sample);
	public void loadCodebook(T codebook);
	public void loadCodebook(String location);
	public void saveCodebook(String location);
	public T getCodebook();
}

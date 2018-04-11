package com.example.android.printersettings;

public class ARGBSettings {
	
	private int a, r, g, b;

	public ARGBSettings() {
		a=0xff;
		r=0xff;
		g=0xff;
		b=0xff;
	}
	
	public void setARGB(int aa, int rr, int gg, int bb)
	{
		a=aa;
		r=rr;
		g=gg;
		b=bb;
	}
	
	public int getA()
	{
		return a;
	}
	
	public int getR()
	{
		return r;
	}
	
	public int getG()
	{
		return g;
	}
	
	public int getB()
	{
		return b;
	}
	
	public void setR(int x)
	{
		r=x;
	}
	
	public void setA(int x)
	{
		a=x;
	}
	
	public void setG(int x)
	{
		g=x;
	}
	
	public void setB(int x)
	{
		b=x;
	}
	
	public void setR(String x)
	{
		if (x!=null)
		{
			setR(Integer.parseInt(x));
		}
	}
	
	public void setA(String x)
	{
		if (x!=null)
		{
			setA(Integer.parseInt(x));
		}
	}
	
	public void setG(String x)
	{
		if (x!=null)
		{
			setG(Integer.parseInt(x));
		}
	}
	
	public void setB(String x)
	{
		if (x!=null)
		{
			setB(Integer.parseInt(x));
		}
	}
	

}

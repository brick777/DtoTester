package net.andrzejczak.dto.tester;

import java.lang.reflect.Method;

public class GetterSetterContainer {

	private Method getter;
	private Method setter;

	public Method getGetter() {
		return getter;
	}

	public void setGetter(final Method getter) {
		this.getter = getter;
	}

	public Method getSetter() {
		return setter;
	}

	public void setSetter(final Method setter) {
		this.setter = setter;
	}
}

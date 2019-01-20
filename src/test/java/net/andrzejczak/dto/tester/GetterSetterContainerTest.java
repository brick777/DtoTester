package net.andrzejczak.dto.tester;

import java.lang.reflect.Method;

public class GetterSetterContainerTest extends DtoTester<GetterSetterContainer> {

	public GetterSetterContainerTest(){

		addCustomMapper(Method.class, () -> {
			try {
				return GetterSetterContainer.class.getMethod("getGetter");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	@Override
	public GetterSetterContainer getDtoClassInstance() {
		return new GetterSetterContainer();
	}
}

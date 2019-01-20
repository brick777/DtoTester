package net.andrzejczak.dto.tester;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DtoTester<T> {

	static {
		final ImmutableMap.Builder<Class<?>, Supplier<?>> typeMapperBuilder = ImmutableMap.builder();

		typeMapperBuilder.put(SortedSet.class, () -> Collections.emptySortedSet());
		typeMapperBuilder.put(Map.class, () -> Collections.emptyMap());
		typeMapperBuilder.put(Set.class, () -> Collections.emptySet());
		typeMapperBuilder.put(SortedMap.class, () -> Collections.emptySortedMap());
		typeMapperBuilder.put(List.class, () -> Collections.emptyList());

		typeMapperBuilder.put(long.class, () -> 0l);
		typeMapperBuilder.put(int.class, () -> 0);
		typeMapperBuilder.put(short.class, () -> (short) 0);
		typeMapperBuilder.put(byte.class, () -> (byte) 0);
		typeMapperBuilder.put(char.class, () -> (char) 0);
		typeMapperBuilder.put(boolean.class, () -> true);
		typeMapperBuilder.put(Date.class, () -> new Date());
		typeMapperBuilder.put(double.class, () -> 0.0d);
		typeMapperBuilder.put(float.class, () -> 0.0f);

		typeMapperBuilder.put(Integer.class, () -> Integer.valueOf(0));
		typeMapperBuilder.put(Long.class, () -> Long.valueOf(0));
		typeMapperBuilder.put(Float.class, () -> Float.valueOf(0.0f));
		typeMapperBuilder.put(Boolean.class, () -> Boolean.TRUE);
		typeMapperBuilder.put(Double.class, () -> Double.valueOf(0.0));
		typeMapperBuilder.put(Character.class, () -> Character.valueOf((char) 0));
		typeMapperBuilder.put(Byte.class, () -> Byte.valueOf((byte) 0));
		typeMapperBuilder.put(BigDecimal.class, () -> BigDecimal.ONE);
		typeMapperBuilder.put(Short.class, () -> Short.valueOf((short) 0));

		DEFAULT_TYPE_MAPPERS = typeMapperBuilder.build();
	}

	private static final ImmutableMap<Class<?>, Supplier<?>> DEFAULT_TYPE_MAPPERS;

	private final Map<Class<?>, Supplier<?>> mappers = new HashMap<>();

	private Set<String> ignoredFieldsSet;

	public DtoTester() {
		mappers.putAll(DEFAULT_TYPE_MAPPERS);
	}

	/**
	 * Add variable name which should be ignored
	 * @param field
	 */
	public void addIgnoredField(final String field) {
		if(this.ignoredFieldsSet==null){
			ignoredFieldsSet = new HashSet<>();
		}
		this.ignoredFieldsSet.add(field);
	}

	/**
	 * Add variables names which should be ignored
	 * @param ignoredFieldsSet
	 */
	public void addIgnoredField(final Set<String> ignoredFieldsSet) {
		this.ignoredFieldsSet = ignoredFieldsSet;
	}

	/**
	 * Add variable types map to test
	 * @param customTypeMap
	 */
	public void addCustomMapper(final Map<Class<?>, Supplier<?>> customTypeMap) {
		customTypeMap.forEach((cClass, cSupplier) -> {
			mappers.putIfAbsent(cClass, cSupplier);
		});
	}

	/**
	 * Add variable type to test
	 * @param cClass
	 * @param cSupplier
	 */
	public void addCustomMapper(final Class<?> cClass, final Supplier<?> cSupplier) {
		mappers.putIfAbsent(cClass, cSupplier);
	}

	public abstract T getDtoClassInstance();

	@Test
	public void testDTO() throws InvocationTargetException, IllegalAccessException, InstantiationException {
		final T classInstance = getDtoClassInstance();
		List<String> fieldList = getFieldList(classInstance.getClass().getDeclaredFields());

		for (String field : fieldList) {
			GetterSetterContainer getterSetterContainer = findMethodForField(field);
			if (getterSetterContainer != null) {

				Method getter = getterSetterContainer.getGetter();
				Method setter = getterSetterContainer.getSetter();

				Class<?> setterParameterType = setter.getParameterTypes()[0];
				Object setterObject = createValue(setterParameterType);

				setter.invoke(classInstance, setterObject);

				Class<?> getterParameterType = getter.getReturnType();
				Object getterObject = getter.invoke(classInstance);

				if (setterParameterType.isPrimitive()) {
					Assert.assertEquals(setterParameterType, getterParameterType);
					Assert.assertEquals(setterObject, getterObject);
				} else {
					Assert.assertSame(setterParameterType, getterParameterType);
					Assert.assertSame(setterObject, getterObject);
				}

			}
		}
	}

	private Object createValue(final Class<?> parameterType) throws IllegalAccessException, InstantiationException {
		final Supplier<?> supplier = this.mappers.get(parameterType);

		if (supplier != null) {
			return supplier.get();
		}

		if (parameterType.isEnum()) {
			if (parameterType.getEnumConstants().length > 0) {
				return parameterType.getEnumConstants()[0];
			} else {
				return null;
			}
		}

		return parameterType.newInstance();
	}

	private List<String> getFieldList(final Field[] fields) {
		return Arrays.asList(fields).stream().filter(field -> {
			if (ignoredFieldsSet == null) {
				return true;
			} else {
				return !ignoredFieldsSet.contains(field.getName());
			}
		}).map(field -> field.getName()).collect(Collectors.toList());
	}

	private GetterSetterContainer findMethodForField(final String fieldName) {
		final Method[] methods = getDtoClassInstance().getClass().getMethods();
		GetterSetterContainer getterSetterContainer = new GetterSetterContainer();

		long methodCount = Arrays.asList(methods).stream()
				.filter(method -> method.getName().toLowerCase().endsWith(fieldName.toLowerCase())).count();
		if (methodCount != 2) {
			return null;
		}

		for (Method method : methods) {
			if (method.getName().toLowerCase().endsWith(fieldName.toLowerCase())) {
				if (method.getGenericReturnType() != Void.TYPE) {
					getterSetterContainer.setGetter(method);
				} else {
					getterSetterContainer.setSetter(method);
				}
			}
		}

		return getterSetterContainer;
	}

}

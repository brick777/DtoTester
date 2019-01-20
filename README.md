# DtoTester
This package provides simple way to implement tests for all DTO classes in your projects.

A Data Transfer Object (DTO) is an object used to pass typed data between layers in your application. By testing these classes, you can increase the coverage of your code.

# How it works
It use reflection to fetch DTO class fields. Then are testing only this methods which are occur in pairs.

# How to 
Assume that our DTO class name is MyDto.
To run test for this class you have to create corresponding class in test package.
```java
public class MyDtoTest extends DtoTester<MyDto> {
  @Override
	public MyDto getDtoClassInstance() {
		return new MyDto();
	}
}
```

# Customization
You can also customize test parameters such as `ignoredFieldList` or `customTypeMap`.

## Ignore some variable
```java
public class MyDtoTest extends DtoTester<MyDto> {

	public MyDtoTest() {
  
		final Set<String> ignoredFieldList = new HashSet<>();
		ignoredFieldList.add("activeTypeEnum");
		ignoredFieldList.add("isActive");
		addIgnoredField(ignoredFieldList);
    
		// and/or
		addIgnoredField("id");
		addIgnoredField("isRed");

	}

	@Override
	public MyDto getDtoClassInstance() {
		return new MyDto();
	}
}
```

## Add custom variable type
```java
public class MyDtoTest extends DtoTester<MyDto> {

	public MyDtoTest() {
  
		final Map<Class<?>, Supplier<?>> customTypeMap = new HashMap<>();

		customTypeMap.put(Method.class, () -> {
			try {
				return MyDto.class.getMethod("getSomeObject");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			}
		});

		addCustomMapper(customTypeMap);

		// and/or 
		addCustomMapper(Method.class, () -> {
			try {
				return MyDto.class.getMethod("getAnotherObject");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			}
		});

	}

	@Override
	public MyDto getDtoClassInstance() {
		return new MyDto();
	}
}
```

# Version History

## 1.0 

Initial release.

ProcessLifecycleOwner/Robolectric issues
========================================
This is a sample project demonstrating issues with robolectric unit tests and the `ProcessLifecycleOwner`.


Background:
-----------
In an app, the `ProcessLifecycleOwner` hooks into activity lifecycles and reports on the global lifecycle of the application (simplified summary: if any activity is resumed, the process lifecycle is `RESUMED`, if no activities have been launched, the process lifecycle is `CREATED`...). An application may rely on `ProcessLifecycleOwner` if it has logic to not execute a particular task unless the app has at least one visible activity, for example.

In an app, the `ProcessLifecycleOwner` is registered in a `<provider>` called `ProcessLifecycleOwnerInitializer`. This provider snippet in the manifest is generated by the build tools. In robolectric tests, this provider isn't launched by default.

The first hack is to "install" the `ProcessLifecycleOwner` class: make it so that this singleton will hook into activity lifecycles and correctly report on the overall lifecycle of the application.  This could be done by registering the `ProcessLifecycleOwnerInitializer` content provider with robolectric, or by executing the same code the `ProcessLifecycleOwnerInitializer` itself does. Our hacky `ProcessLifecycleTrojan` class does the latter, in its `setup()` function.

`ProcessLifecycleOwner` is a singleton, and thus maintains a static state across tests. The second hack is to use reflection to make sure we have a fresh new instance of `ProcessLifecycleOwner` for each test, by resetting its static `sInstance` field, in `ProcessLifecycleTrojan.tearDown()`.

This second hack no longer works in Robolectric 4.3. It fails with this exception:
```
java.lang.IllegalAccessException: Can not set static final androidx.lifecycle.ProcessLifecycleOwner field androidx.lifecycle.ProcessLifecycleOwner.sInstance to androidx.lifecycle.ProcessLifecycleOwner

	at sun.reflect.UnsafeFieldAccessorImpl.throwFinalFieldIllegalAccessException(UnsafeFieldAccessorImpl.java:76)
	at sun.reflect.UnsafeFieldAccessorImpl.throwFinalFieldIllegalAccessException(UnsafeFieldAccessorImpl.java:80)
	at sun.reflect.UnsafeQualifiedStaticObjectFieldAccessorImpl.set(UnsafeQualifiedStaticObjectFieldAccessorImpl.java:77)
	at java.lang.reflect.Field.set(Field.java:764)
	at androidx.lifecycle.ProcessLifecycleTrojan.tearDown(ProcessLifecycleTrojan.kt:61)
	at com.example.processlifecycleownerpersistedstate.ExampleUnitTest.tearDown(ExampleUnitTest.kt:29)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:33)
```

Problem details:
----------------

Steps to reproduce the various issues:

**Problem 1: `ProcessLifecyleOwner` not setup: not reporting resumed state for a resumed activity**
* Keep robolectric 4.2.1 (default in this project)
* Comment out the `setup()` and `tearDown()` functions in `ExampleUnitTest`, to not use the `ProcessLifecycleTrojan` hack at all.
* Run the tests.
* Expected behavior: the tests pass.
* Actual behavior:
  * The tests fail as the first assertion that the state should be `CREATED` (it's in fact `INITIALZED`)
  * Comment out that first assertion, and the second assertion fails: We expect the state to be `RESUMED` but it's `INITIALIZED` still.
 
**Problem 2: `ProcessLifecycleOwner` setup, but not reset after each test**
* Keep robolectric 4.2.1 (default in this project)
* Keep the `ExampleUnitTest.setup()` function to hook `ProcessLifecycleOwner` into activity lifecycles
* Comment out the `ExampleUnitTest.tearDown()` function to remove the reflection hack that resets the `ProcessLifecycleOwner` singleton.
* Comment out the last three lines of `ExampleUnitTest.testLaunchActivity()` which destroy the activity
* Run the tests.
* Expected behavior: the tests pass
* Actual behavior:
  * `testLaunchActivity1()` passes, but `testLaunchActivity2()` fails. Its first assertion fails: We expect the app state to be `CREATED`, but it's `RESUMED` (left over from the first test).

**Problem 3: Can't reset the `ProcessLifecycleOwner` state for each test, with robolectric 4.3**
* Update to robolectric 4.3
* Put back any lines you have have commented out while testing the previous problems.
* Run the tests.
* Expected behavior: the tests pass
* Actual behavior: `IllegalAccessException` in `ProcessLifecycleTrojan.tearDown()`


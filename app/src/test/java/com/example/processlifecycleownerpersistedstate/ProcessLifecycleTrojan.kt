// this is a hack, we need to access package-protected LifecycleDispatcher
@file:Suppress("PackageDirectoryMismatch")

package androidx.lifecycle

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.robolectric.util.ReflectionHelpers

/**
 * We need the process-level lifecycle states to work (our app queries the state and has logic which depends on it)
 * In a real app, the ProcessLifecycleOwner is installed via a ContentProvider (which google itself
 * calls a trojan). It's present in the generated AndroidManifest.xml:
 *
 *     <provider
 *         android:name="android.arch.lifecycle.ProcessLifecycleOwnerInitializer"
 *         android:authorities="lifeisbetteron.com.test.lifecycle-trojan"
 *         android:exported="false"
 *         android:multiprocess="true" />
 *
 *  Robolectric doesn't install providers by default.
 *  We need to find a way to install the ProcessLifecycleOwner for tests.
 *  One way is to install the content provider with robolectric:
 *
 *      ProviderInfo providerInfo = new ProviderInfo();
 *      providerInfo.exported = false;
 *      providerInfo.authority = BuildConfig.APPLICATION_ID + ".lifecycle-trojan";
 *      providerInfo.name = "android.arch.lifecycle.ProcessLifecycleOwnerInitializer";
 *      providerInfo.multiprocess = true;
 *      Robolectric.buildContentProvider(ProcessLifecycleOwnerInitializer.class).create(providerInfo);
 *
 *  Another way is to instantiate the relevant classes directly.
 *
 *  Here we take the second approach: instantiating the classes directly. We want to avoid
 *  overhead for tests (the test suite is already quite long to execute). Also, by instantiating
 *  the classes directly, we can avoid some (but not all) cleanup. The two relevant classes,
 *  LifecycleDispatcher and ProcessLifecycleOwner each maintain state, which we can't persist between
 *  tests. We need a fresh environment in each test.
 *  By instantiating LifecycleDispatcher directly here, we avoid having to reset its static field
 *  sInitialized.
 *  Unfortunately we still need to reset ProcessLifecycleOwner.sInstance, and we need reflection
 *  for that :(.
 */
object ProcessLifecycleTrojan {
    fun setup() {
        // This is basically what ProcessLifecycleOwnerInitializer (the trojan ContentProvider) does:
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.registerActivityLifecycleCallbacks(LifecycleDispatcher.DispatcherActivityCallback())
        (ProcessLifecycleOwner.get() as ProcessLifecycleOwner).attach(context)
    }

    fun tearDown() {
        // Hackity hackity hack hack hack
        // The androidx process lifecycle classes maintain static state that needs to be reset
        // between tests.
        val instance = ReflectionHelpers.callConstructor(ProcessLifecycleOwner::class.java)
        ReflectionHelpers.setStaticField(ProcessLifecycleOwner::class.java, "sInstance", instance)
    }
}


package im.status.ethereum;

import android.support.multidex.MultiDexApplication;
import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import org.devio.rn.splashscreen.SplashScreenReactPackage;
import com.facebook.react.ReactApplication;
import community.revteltech.nfc.NfcManagerPackage;
import io.invertase.firebase.RNFirebasePackage;
import io.invertase.firebase.messaging.RNFirebaseMessagingPackage;
import io.invertase.firebase.notifications.RNFirebaseNotificationsPackage;
import net.rhogan.rnsecurerandom.RNSecureRandomPackage;
import com.ocetnik.timer.BackgroundTimerPackage;
import com.horcrux.svg.SvgPackage;
import com.lugg.ReactNativeConfig.ReactNativeConfigPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.alinz.reactnativewebviewbridge.WebViewBridgePackage;
import com.AlexanderZaytsev.RNI18n.RNI18nPackage;
import org.reactnative.camera.RNCameraPackage;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import com.rnfs.RNFSPackage;
import fr.bamlab.rnimageresizer.ImageResizerPackage;
import im.status.ethereum.module.StatusPackage;
import io.realm.react.RealmReactPackage;
import me.alwx.HttpServer.HttpServerReactPackage;
import com.oblador.keychain.KeychainPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import im.status.ethereum.function.Function;

public class MainApplication extends MultiDexApplication implements ReactApplication {

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            boolean devCluster = false;
            if (BuildConfig.ETHEREUM_DEV_CLUSTER == "1") {
                devCluster = true;
            }

            boolean webViewDebugEnabled = false;
            if (BuildConfig.DEBUG_WEBVIEW == "1") {
                webViewDebugEnabled = true;
            }

            StatusPackage statusPackage = new StatusPackage(BuildConfig.DEBUG, devCluster);
            Function<String, String> callRPC = statusPackage.getCallRPC();
            List<ReactPackage> packages = new ArrayList<ReactPackage>(Arrays.asList(
                    new MainReactPackage(),
                    new NfcManagerPackage(),
                    new RNFirebasePackage(),
                    new RNFirebaseMessagingPackage(),
                    new RNFirebaseNotificationsPackage(),
                    new RNSecureRandomPackage(),
                    new BackgroundTimerPackage(),
                    new SvgPackage(),
                    new HttpServerReactPackage(),
                    new SplashScreenReactPackage(),
                    statusPackage,
                    new RealmReactPackage(),
                    new RNI18nPackage(),
                    new RNCameraPackage(),
                    new RNFSPackage(),
                    new ReactNativeDialogsPackage(),
                    new ImageResizerPackage(),
                    new PickerPackage(),
                    new WebViewBridgePackage(webViewDebugEnabled, callRPC),
                    new ReactNativeConfigPackage(),
                    new KeychainPackage()));

            return packages;
        }

        @Override
        protected String getJSMainModuleName() {
            return "index.android";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
    }
}

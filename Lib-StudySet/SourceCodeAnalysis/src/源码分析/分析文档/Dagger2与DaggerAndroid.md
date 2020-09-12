## 关于dagger2+dagger.android
### Dagger2：Dagger2 的使命就是为了给需求者注入依赖。
参考链接：<a href="https://blog.csdn.net/briblue/article/details/75578459">轻松学，听说你还没有搞懂 Dagger2</a>

1. @Inject和@Component
    ```
    @Inject 注解就如同一个标签，或者说它是一个记号，它是给 Dagger2 看的。它运用的地方有两处。
        @Inject 给一个类的相应的属性做标记时，说明了它是一个依赖需求方，需要一些依赖。
        @Inject 给一个类的构造方法进行注解时，表明了它能提供依赖的能力。
        
    就这样，通过 @Inject 注解符号，就很容易标记依赖和它的需求方。但是，单单一个 @Inject 是不能让 Dagger2 正常运行的。还需要另外一个注解配合。这个注解就是 @Component。
    
    @Component 相当于联系纽带，将 @inject 标记的需求方和依赖绑定起来，并建立了联系，而 Dagger2 在编译代码时会依靠这种关系来进行对应的依赖注入。
    需要注意的是，Component 中方法除了可以返回类型，还可以在方法中传入类型参数。目的是针对这个参数对象进行依赖注入(通过调用接口中的方法给 XXX 进行依赖注入，即该类中有被@Inject注解的成员)。 
    
   注：生成类中包含 n个@Inject注解的 XXX_Factory类 + 1个@Component注解的 DaggerXXX类 + @Component注解类中方法数个 XXX_MembersInjector类
    ```
2. @Provides 和 @Module
    ```
    @Provide 本身的字面意思就是提供，显然在 Dagger2 中它的作用就是提供依赖。 
    @Module 是模块的意思，Dagger2 中规定，用 @Provides 注解的依赖必须存在一个用 @Module 注解的类中。 
   
    为什么需要@Provides 和 @Module？
    因为单纯使用@Inject时，@Inject不仅需要修饰注入成员也必须要修饰该成员类的构造方法。
    若我们是应用的第三方工具包，我们是无法修改其源码的，这时候我们可以用 @Module 、@Provides来帮忙。
    其实Module 其实是一个简单工厂模式，Module 里面的方法都是创建相应类实例的方法。即@Provides 和 @Module实际上是用来代替@Inject的第二个作用的。
    
    优先级：Dagger2 依赖查找的顺序是先查找 Module 内所有的 @Provides 提供的依赖，如果查找不到再去查找 @Inject 提供的依赖。
    ```
3. @Singleton 和 @Scope
    ```
    @Singleton Dagger2 利用其提供单例设计。用 @Singleton 标注在目标单例上，然后用 @Singleton 标注在 Component 对象上。
    如果要以 @Provides 方式提供单例的话，需要用 @Singleton 注解依赖提供的方法。
    
    @Scope 作用域。@Singleton 只是 @Scope 一个默认的实现而已，但是因为它更具可读性，能够让开发者一眼就明白它的作用是为了单例。
    @Singleton 所拥有的单例能力是以 Component 为范围的限定的。  
    ```
4. @Qualifiers 和 @Name
    ```
    @Qualifiers 是修饰符的意思。@Name 只是被 @Qualifier 注解的一个注解，它能够有效完全是因为 @Qualifier。
    
    在一个 Module 中 @Provides 提供的依赖是由返回值决定的。这样就会出现问题，同一种类型不同实例，怎么去区别？
    Dagger2 给出了解决方案。用 @Name 注解就好了，配合 @Inject 和 @Provides 一起使用。 
    ```
### Dagger.Android
参考链接：<a href="https://www.jianshu.com/p/2ec39d8b7e98">Android开发之dagger.android--Activity</a>
#### 官网dagger.android 的使用步骤
1. 在 AppComponent 中注入 AndroidInjectionModule，以确保Android的类（Activity、Fragment、Service、BroadcastReceiver及ContentProvider等）都可以被绑定。
    ```
    @Singleton
    @Component(modules = [AppModule::class, AndroidInjectionModule::class])
    interface AppComponent { void inject(MyApplication application); } 
    ```
2. 创建子组件 - DaggerAndroidActSubComponent：其继承自 AndroidInjector<YourActivity>，在该子组件中含有一个抽象类Builder， 该Builder继承自AndroidInjector.Builder，并由@Subcomponent.Builder注解。
    ```
    @Subcomponent(modules = AndroidInjectionModule.class)
    public interface DaggerAndroidActSubComponent extends AndroidInjector<DaggerAndroidActivity>{
        @Subcomponent.Builder
         abstract class Builder extends AndroidInjector.Builder<DaggerAndroidActivity>{
    
        }
    }
    ```
3. 创建Module - DaggerAndroidActModule：在定义了子组件DaggerAndroidActSubComponent后，定义该子组件的Module - DaggerAndroidActModule，
并绑定DaggerAndroidActSubComponent.Builder，然后将DaggerAndroidActModule添加到组件层次中。在DaggerAndroidActModule里，提供了一个获取Student实例的方法。
    ```
    @Module(subcomponents = DaggerAndroidActSubComponent.class)
    public abstract class DaggerAndroidActModule {
    
        @Binds
        @IntoMap
        @ActivityKey(DaggerAndroidActivity.class)
        abstract AndroidInjector.Factory<? extends Activity> bindDaggerAndroidActivityInjectorFactory(DaggerAndroidActSubComponent.Builder builder);
    
        @Provides
        @Singleton
        static Student provideStudent() {
            return new Student();
        }
    }
    ```
4. 将上一步定义的DaggerAndroidActModule 添加到Application Component中。
    ```
    @Singleton
    @Component(modules = {AndroidInjectionModule.class, AppModule.class, DaggerAndroidActModule.class})
    public interface AppComponent {
        void inject(MyApplication application);
    } 
    ```
5. 自定义Application，并实现HasActivityInjector接口，同时注入(@Inject)DispatchingAndroidInjector<Activity>实例，该实例在activityInjector()方法中返回。
    ```
    public class MyApplication extends Application implements HasActivityInjector{
    
        @Inject
        DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    
        @Override
        public void onCreate() {
            super.onCreate();
            DaggerAppComponent.create()
                    .inject(this);
        }
    
        @Override
        public AndroidInjector<Activity> activityInjector() {
            return dispatchingActivityInjector;
        }
    }
    ```
6. 在DaggerAndroidActivity.onCreate()方法中，在调用super.onCreate()之前调用AndroidInjection.inject(this)。
    ```
    public class DaggerAndroidActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            AndroidInjection.inject(this);
            super.onCreate(savedInstanceState);

        }
    }
    ```
我们可以注意到这会创建很多DaggerXXXActivityModule和DaggerXXXActivitySubComponent的java文件，好像也没有起到优化作用。

官网上有一个提示：如果您的subcomponent 及其构建器没有第2步中提到的其他方法或超类型，您可以使用@ContributesAndroidInjector为您生成它们。我们就不需要步骤2和3，取而代之的是添加一个抽象模块方法，该方法返回您的activity，使用@ContributesAndroidInjector对其进行注解，并指定要安装到子组件中的模块。 如果子组件需要scopes，则也可以用@scopes注解到该方法。

#### 优化后的效果
1. 新建一个AllActivitysModule，在这里提供所有的ActivityModule，每个都用@ContributesAndroidInjector注解。
    ```
    @Module(subcomponents = DaggerAndroidActSubComponent.class)
    public abstract class AllActivitysModule {
    
        @ContributesAndroidInjector(modules = DaggerAndroidActModule.class)
        abstract DaggerAndroidActivity contributeDaggerAndroidActivityInjector();
    
    }
    ```
2. 修改DaggerAndroidActModule，变成一个普通的Module，和前文对比，删掉了(subcomponents = DaggerAndroidActSubComponent.class)以及bindDaggerAndroidActivityInjectorFactory()抽象方法。在这里可以根据需要提供实例。
    ```
    @Module
    public abstract class DaggerAndroidActModule {
        @Provides
        static XXX provideXXX() {
            return new XXX();
        }
    }
    ```
3. 修改AppComponent，删除原来的DaggerAndroidActModule,把AllActivitysModule.class放进modules中。
    ```
    @Singleton
    @Component(modules = {AndroidInjectionModule.class, AppModule.class, AllActivitysModule.class})
    public interface AppComponent {
        void inject(MyApplication application);
    }
    ```
#### 我们项目中所使用的效果

上面的第5步也可以修改为直接继承自DaggerApplication

```
class MyApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val builder = DaggerApplicationComponent.builder()
        builder.module(ApplicationModule(this))
        return builder.create()
    }
}

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent : AndroidInjector<MyApplication> {

    @Component.Builder
    interface Builder {
        fun module(appModule: ApplicationModule): ApplicationComponent.Builder

        fun create(): ApplicationComponent
    }
}

@Module(includes = arrayOf(AndroidSupportInjectionModule::class, NetModule::class, UIInjectModule::class))
class ApplicationModule(var mApplication: MyApplication) {

    @Singleton
    @ForApplication
    @Provides
    fun providerApplicationContext(): Context {
        return mApplication
    }

    @Singleton
    @Provides
    fun providerMyDatabase(@ForApplication context: Context): MyDatabase {
        return MyDatabase.getInstance(context)
    }
}

@Module
class NetModule {

    @Singleton
    @Provides
    fun provideBaseUrl(@ForApplication context: Context): HttpUrl {
        ...
    }
    
    @Singleton
    @Provides
    fun providerGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(AppResult::class.java, AppResultDeser())
        ......
        gsonBuilder.setLenient()
        return gsonBuilder.create()
    }

    @Singleton
    @Provides
    fun providerOkHttpClient(@ForApplication context: Context): OkHttpClient {
        val okHttpClient = HttpManager.getDefault(context).okHttpClient
        val cacheDir = File(context.codeCacheDir, "network")
        val cache = Cache(cacheDir, 10 * 1024 * 1024)
        val builder = okHttpClient.newBuilder()
        return builder
                .addNetworkInterceptor(LogInterceptor())
                .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .readTimeout(10 * 1000, TimeUnit.MILLISECONDS)
//                .cookieJar(cookieJar)
                .cache(cache)
                .build()
    }
    
    @Singleton
    @Provides
    fun providerRetrofit(url: HttpUrl, httpClient: OkHttpClient, gson: Gson): Retrofit {
        val newBuilder = httpClient.newBuilder()
        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(url)
                .client(newBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofitBuilder.build()
    }

    @Singleton
    @Provides
    fun providerNetService(retrofit: Retrofit): NetService {
        return retrofit.create(NetService::class.java)
    }
}

@Module
abstract class UIInjectModule {

    @ContributesAndroidInjector
    abstract fun myFragment(): MyFragment

    @ContributesAndroidInjector
    abstract fun myBaseActivity(): MyBaseActivity
    
    ......
}

@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ForApplication
```

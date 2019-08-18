package com.android.javalib;

import org.reactivestreams.Subscription;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class MainClass {
    private static long count;

    public static void main(String[] args) {

        String ad = "-1000";

        System.out.println(Integer.parseInt(ad)+"");
//        makeFutureOfCallable();

//        fliterMethod();

//        otherMethod();

//        flowableCreate();

//        System.out.println(StaticClass.createTestClass());
//        System.out.println(StaticClass.createTestClass());

//        new ParentClass();
//        ChlidClass chlidClass1 = new ChlidClass();
//        ChlidClass chlidClass2 = new ChlidClass();
//        chlidClass1.list.add("123");
//        chlidClass2.list.add("456");
//        System.out.println(chlidClass1);
//        System.out.println(chlidClass2);

//        List<String> list = new ArrayList<>();
//        list.add("aaa");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(list.size());
//            }
//        }).start();
//        System.out.println(System.currentTimeMillis());
//        for (int i = 0; i < 10000000; i++) {
//            list.add("bbb");
//        }
//        System.out.println(System.currentTimeMillis());

//        for (int i = 0;i<DeviceCodeDataList.getDevCodeList().size() ; i++){
//            System.out.println("stringFutureTask == " + DeviceCodeDataList.getDevCodeList().get(i));
//        }

        ProxyInterface real = new RealClass();
        ProxyInterface proInterface = (ProxyInterface) Proxy
                .newProxyInstance(real.getClass().getClassLoader()
                        ,RealClass.class.getInterfaces(), new TestInvacationHandler(real));
        proInterface.handlingEvents();

    }

    private static void flowableCreate() {
        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                emitter.onNext("yes1");
                emitter.onNext("yes2");
                emitter.onNext("yes3");
                emitter.onNext("yes4");
                emitter.onNext("no");
                emitter.onNext("yes5");
            }
        }, BackpressureStrategy.DROP)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !s.contains("no");
                    }
                })
                .subscribe(new FlowableSubscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private static void otherMethod() {
        Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                return Observable.just("");
            }
        })
                .share().subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        Single.just(1).subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Integer integer) {

            }

            @Override
            public void onError(Throwable e) {

            }
        });

        //数组长度5
        String[] args1 = new String[]{"name", "age", "like", "lalala"};
        //数组长度4
        String[] args2 = new String[]{"晓初", "24", "eat"};
        HashMap<String, String> hashMap = new HashMap<>();
        //相同的数组可以进行合并
        Observable.zip(Observable.fromArray(args1), Observable.fromArray(args2), new BiFunction<String, String, HashMap<String, String>>() {
            @Override
            public HashMap<String, String> apply(String s, String s2) throws Exception {
                hashMap.put(s, s2);
                return hashMap;
            }
        }).subscribe(new Observer<HashMap<String, String>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(HashMap<String, String> stringStringHashMap) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private static void fliterMethod() {
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 0).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return integer + "";
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return Observable.just(s);
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                System.out.println("stringFutureTask == " + s + "");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private static void makeFutureOfCallable() {
        FutureTask<String> stringFutureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(5000);
                return "I am Callable";
            }
        });
        ThreadPoolUtils.getInstance().execute(stringFutureTask);
        try {
            // get()函数会阻塞，直到结果返回
            System.out.println("stringFutureTask == " + stringFutureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

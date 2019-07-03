源码分析第...几篇，算了这些不重要，以往使用Fragment都比较传统，最近发现Fragment原来还有这么多用法，这也导致了我好像Fragment的源码实现的原因，趁着最近不是很忙“燥起来”。

我们日常使用Fragment无非就几种：replace和add、hide、show，以下面这种为例：
```
Fragment testFragment=new TestFragment();
//获取FragmentManager
FragmentManager fm = getFragmentManager();// 或者getSupportFragmentManager();
//获取FragmentTransaction
FragmentTransaction ft = fm.beginTransaction();
//进行添加操作
ft.add(R.id.fragment_container,testFragment);
// 显示
ft.show(testFragment);
//提交
ft.commit();
```
那我们接下来就按照以上这种顺序来分析一下Fragment的加载过程。

## FragmentManager fm = getSupportFragmentManager();
先看一下使用getFragmentManager()方法获取FragmentManager
```
// Activity类：
    @Deprecated
    public FragmentManager getFragmentManager() {
        return mFragments.getFragmentManager();
    }
```
由于本文是以android-28为基础的源码分析，而在api28中getFragmentManager()这种方式已被弃用，官方建议使用官方建议使用android.support.v4.app.Fragment来替代android.app.Fragment，使用android.support.v4.app.FragmentManager来替代android.app.FragmentManager，因此我们本文以getSupportFragmentManager()方法获取FragmentManager来继续分析。而使用getSupportFragmentManager()方法当前Activity就必须继承于FragmentActivity以及其子类。
```
// FragmentActivity类：
    final FragmentController mFragments = FragmentController.createController(new FragmentActivity.HostCallbacks());
    
    public FragmentManager getSupportFragmentManager() {
        return this.mFragments.getSupportFragmentManager();
    }
    // 内部类
    class HostCallbacks extends FragmentHostCallback<FragmentActivity> {
        public HostCallbacks() {
            super(FragmentActivity.this);
        }
        ......
    }
    
// FragmentController类：
    private final FragmentHostCallback<?> mHost;

    public static FragmentController createController(FragmentHostCallback<?> callbacks) {
        return new FragmentController(callbacks);
    }

    private FragmentController(FragmentHostCallback<?> callbacks) {
        this.mHost = callbacks;
    }
    
    public FragmentManager getSupportFragmentManager() {
        return this.mHost.getFragmentManagerImpl();
    }
    
// FragmentHostCallback类：
    FragmentHostCallback(@NonNull FragmentActivity activity) {
        // 关注一下第三个参数activity.mHandler
        this(activity, activity, activity.mHandler, 0);
    }

    FragmentHostCallback(@Nullable Activity activity, @NonNull Context context, @NonNull Handler handler, int windowAnimations) {
        // 初始化了FragmentManagerImpl类
        this.mFragmentManager = new FragmentManagerImpl();
        this.mActivity = activity;
        this.mContext = (Context)Preconditions.checkNotNull(context, "context == null");
        // 初始化了Handler，该mHandler会在commit操作中用到
        this.mHandler = (Handler)Preconditions.checkNotNull(handler, "handler == null");
        this.mWindowAnimations = windowAnimations;
    }
    
    FragmentManagerImpl getFragmentManagerImpl() {
        return this.mFragmentManager;
    }
    
// FragmentManagerImpl类：
    final class FragmentManagerImpl extends FragmentManager implements Factory2
```
哈哈，为了方便就直接将该部分相关代码直接放在一起啦，不要慌，我们一步一步讲起：

1. getSupportFragmentManager()方法中，出现了mFragments
2. mFragments实际为FragmentController，并且将FragmenActivity的内部类HostCallbacks通过构造参数传入该类中，并保存到成员变量mHost中，注意该成员后续会讲到
3. HostCallbacks为FragmentActivity的内部类，其继承于类FragmentHostCallback。并且HostCallbacks构造方法中也同时调用了其父类FragmentHostCallback的构造方法，并初始化了类FragmentManagerImpl，而FragmentManagerImpl继承于FragmentManager
4. 回到第1步的方法getSupportFragmentManager()中，该方法里调用了mFragments.getSupportFragmentManager()方法
5. mFragments.getSupportFragmentManager()方法里又调用了本部分的最后一步mHost.getFragmentManagerImpl()方法中，第2步讲到后面会使用。并且最后返回第3步所说的实例化的FragmentManagerImpl类

通过上面展示的代码以及所描述的5步说明，我们可以很清晰的看到mFragments被final修饰而且又是类成员，因此一个FragmentActivity对应一个FragmentController、一个FragmentHostCallback和一个FragmentManagerImpl。
## FragmentTransaction ft = fm.beginTransaction();
从上面部分，我们知道了fm其实就是FragmentManagerImpl类，所以我们第二部分就是调用fm.beginTransaction()方法：
```
// FragmentManagerImpl类:
    public FragmentTransaction beginTransaction() {
        return new BackStackRecord(this);
    }
    
// BackStackRecord类：
    final class BackStackRecord extends FragmentTransaction
                    implements BackStackEntry, OpGenerator {
        ......
        public BackStackRecord(FragmentManagerImpl manager) {
            this.mManager = manager;
        }
        ...
    }
```
这部分显的很单调，没错，这部分就这些内容，其实就是返回了继承于FragmentTransaction的BackStackRecord类。注意啦BackStackRecord类很重要，非常重要。本文以android-28为标准而讲解，而本类在28中修改很大，28之前的版本BackStackRecord实现了Runnable接口，具体的可自行查看。该类直译过来可被称为：返回堆栈记录，此后的有关fragment的基本所有操作都是通过它来完成，我们后续再详细说明。
## ft.add(R.id.fragment_container,testFragment);
在第二部分中我们知道ft指的是BackStackRecord类，我们来看一下它的add方法：
```
// BackStackRecord类:
    ArrayList<BackStackRecord.Op> mOps = new ArrayList();

    public FragmentTransaction add(Fragment fragment, @Nullable String tag) {
        this.doAddOp(0, fragment, tag, 1);
        return this;
    }
    
    private void doAddOp(int containerViewId, Fragment fragment, @Nullable String tag, int opcmd) {
        // 获取fragment的Class对象
        Class fragmentClass = fragment.getClass();
        // 获取该fragment对象的修饰符
        int modifiers = fragmentClass.getModifiers();
        // 如该fragment为：匿名类或者不是public修饰符标识的类或者(它是内部类且不是静态的)
        if (fragmentClass.isAnonymousClass() || !Modifier.isPublic(modifiers) || fragmentClass.isMemberClass() && !Modifier.isStatic(modifiers)) {
            // 则抛出如下异常
            throw new IllegalStateException("Fragment " + fragmentClass.getCanonicalName() + " must be a public static class to be  properly recreated from" + " instance state.");
        } else {
            fragment.mFragmentManager = this.mManager;
            if (tag != null) {
                // 防止同一个fragment被add多次并且设置了多个不同的tag
                // 如果tag不等于空，并且tag不等于fragment.mTag
                if (fragment.mTag != null && !tag.equals(fragment.mTag)) {
                    // 则抛出如下异常
                    throw new IllegalStateException("Can't change tag of fragment " + fragment + ": was " + fragment.mTag + " now " + tag);
                }

                fragment.mTag = tag;
            }

            if (containerViewId != 0) {
                // 容器视图id，即FrameLayout布局id
                if (containerViewId == -1) {
                    throw new IllegalArgumentException("Can't add fragment " + fragment + " with tag " + tag + " to container view with no id");
                }
                // 方法多次add其containerViewId且设置多个不同的containerViewId
                if (fragment.mFragmentId != 0 && fragment.mFragmentId != containerViewId) {
                    throw new IllegalStateException("Can't change container ID of fragment " + fragment + ": was " + fragment.mFragmentId + " now " + containerViewId);
                }

                fragment.mContainerId = fragment.mFragmentId = containerViewId;
            }

            this.addOp(new BackStackRecord.Op(opcmd, fragment));
        }
    }
    
    void addOp(BackStackRecord.Op op) {
        this.mOps.add(op);
        op.enterAnim = this.mEnterAnim;
        op.exitAnim = this.mExitAnim;
        op.popEnterAnim = this.mPopEnterAnim;
        op.popExitAnim = this.mPopExitAnim;
    }
    
// BackStackRecord内部类Op：
    static final class Op {
        int cmd;//指令：代表对fragment的操作，比如：add==1
        Fragment fragment;//保存要操作的fragment
        // 以下四个为动画设置
        int enterAnim;
        int exitAnim;
        int popEnterAnim;
        int popExitAnim;

        Op() {
        }

        Op(int cmd, Fragment fragment) {
            this.cmd = cmd;
            this.fragment = fragment;
        }
    }
```
再次将此部分有关代码放到了一起，接着一步一步说明一下吧：

1. 从add操作开始接着调用自身的doAddOp(0, fragment, tag, 1)方法，并设置Op中的cmd指令为1
2. doAddOp(0, fragment, tag, 1)方法中判断一些异常情况后设置fragment的成员属性mContainerId和mFragmentId为containerViewId;并且继续执行自身的addOp(new BackStackRecord.Op(opcmd, fragment))方法。
3. addOp(new BackStackRecord.Op(opcmd, fragment))方法中，就是将Op对象加入到mOps集合中，并设置动画。

第三部分是不是也很简单的样子，那我们继续吧！！！
## ft.show(testFragment);
直接上代码：
```
// BackStackRecord类:
    public FragmentTransaction show(Fragment fragment) {
        this.addOp(new BackStackRecord.Op(5, fragment));
        return this;
    }
```
通过第三部分的了解，我们可以很清晰的看出来，show操作其实只是调用addOp(new BackStackRecord.Op(5, fragment))方法，设置一下Op类的cmd指令。

其实上面的四部分都是比较好理解的，接下来将进入最后一部分，也是本文的最复杂最重要的部分
![](https://user-gold-cdn.xitu.io/2019/6/1/16b0ee9a54855512?w=271&h=241&f=png&s=71075)
## ft.commit();
```
// BackStackRecord类:
    public int commit() {
        return this.commitInternal(false); // 注意这个false哦
    }
    
    int commitInternal(boolean allowStateLoss) {
        // 若重复commit，则会抛出此异常
        if (this.mCommitted) {
            throw new IllegalStateException("commit already called");
        } else {
            if (FragmentManagerImpl.DEBUG) {
                Log.v("FragmentManager", "Commit: " + this);
                LogWriter logw = new LogWriter("FragmentManager");
                PrintWriter pw = new PrintWriter(logw);
                this.dump("  ", (FileDescriptor)null, pw, (String[])null);
                pw.close();
            }

            this.mCommitted = true;
            // 这个成员默认是false，只有我们自己调用了addToBackStack方法，才会设置为true
            if (this.mAddToBackStack) {
                this.mIndex = this.mManager.allocBackStackIndex(this);
            } else {
                this.mIndex = -1;
            }
            // 此mManager我们上面第一部分讲解过，实际为FragmentManagerImpl类
            this.mManager.enqueueAction(this, allowStateLoss);
            return this.mIndex;
        }
    }
    
// FragmentManagerImpl类：
    // 注意：参数action为BackStackRecord类，而allowStateLoss为false，上面有说明
    public void enqueueAction(FragmentManagerImpl.OpGenerator action, boolean allowStateLoss) {
        if (!allowStateLoss) {
            // 状态丢失的异常检查（具体请参考：https://www.jianshu.com/p/aa735c60c867）
            // 如果我们提交使用的是commit方法，则会走到该判断里来，因为commit方法allowStateLoss传的是false。如果我们提交使用的是commitAllowingStateLoss方法，则不会走到该判断中来，因为commitAllowingStateLoss方法传入的allowStateLoss为true
            this.checkStateLoss();
        }

        synchronized(this) {
            if (!this.mDestroyed && this.mHost != null) {
                if (this.mPendingActions == null) {
                    this.mPendingActions = new ArrayList();
                }
                // 加入待定任务队列中，mPendingActions是ArrayList
                this.mPendingActions.add(action);
                this.scheduleCommit();
            } else if (!allowStateLoss) {
                throw new IllegalStateException("Activity has been destroyed");
            }
        }
    }
    
    void scheduleCommit() {
        synchronized(this) {
            boolean postponeReady = this.mPostponedTransactions != null && !this.mPostponedTransactions.isEmpty();
            boolean pendingReady = this.mPendingActions != null && this.mPendingActions.size() == 1;
            if (postponeReady || pendingReady) {
                this.mHost.getHandler().removeCallbacks(this.mExecCommit);
                this.mHost.getHandler().post(this.mExecCommit);
            }

        }
    }
```
先贴出该部分的中的部分代码，我们来分析一下：

1. ft.commit()操作实际上调用了BackStackRecord类自身的commitInternal方法，然后判断是否设置了mAddToBackStack，最后又继续调用了FragmentManagerImpl类中的enqueueAction方法继续执行。
2. FragmentManagerImpl类中的enqueueAction方法中有两个参数：action和allowStateLoss，而代码中也说明了allowStateLoss为false(详细请看上面代码部分)，因此我们主要需要关注的还是action这个参数，action实际上是FragmentManagerImpl类中的OpGenerator接口，而实现OpGenerator接口的地方有两个：一个是FragmentManagerImpl类中的内部类PopBackStackState（后面讲“回退栈”时再详细说明），另一个就是我们所熟悉的BackStackRecord类。接着说流程：在enqueueAction方法中将我们要操作的action添加到待定任务队列中，继续执行自身的scheduleCommit()方法。
3. 我们可以从代码中看到scheduleCommit()方法中，调用了Handler的post方法，执行了一个任务mExecCommit，是否还记得mHost是谁？在上面第一部分实例化FragmentHostCallback类时传入了FragmentActivity，继而初始化了mHandler，而mHost正是FragmentHostCallback类型，而mHost.getHandler()返回的正是此时的mHandler。

从上面的三步中我们了解到了，现在逻辑以及到了mExecCommit（Runnable）的run方法里，我们先不急看run()方法，我们回来看一下enqueueAction()方法为什么把action添加进入mPendingActions里？因为从后面的代码中可以了解每次commit后都会将mPendingActions集合清空，那为什么还要使用集合保存呢？不知道别的小伙伴有没有这方面的纠结，我一开始反正是纠结的。那我们来分析一下使用mPendingActions的原因：我们知道了commit的后续操作是在mExecCommit（Runnable）的run方法里，而mExecCommit又是通过Handler当作消息post出去的，因此这里就可以把commit操作当作是在异步中执行的逻辑。这又是什么原因呢？那是因为Handler发出去的消息并不是被Looper马上执行的，而是需要先从消息队列中取出来再去执行，因此在这个空隙，我们可以会多次切换fragment（场景：app的首页是由底部多个Tab+多个fragmnet实现，我们频繁多次切换tab）而导致多次生成action，因此需要一个集合来当作队列将多个action添加进去，在后面统一处理。

这里我们再说一下几种提交操作的方法：
* commit();
* commitAllowingStateLoss();
* commitNow();
* commitNowAllowingStateLoss();

**commit() vs commitAllowingStateLoss()**
> 用commit()提交有时候会遇到IllegalStateException, 说你在onSaveInstanceState()之后提交, 
commit()和commitAllowingStateLoss()在实现上唯一的不同就是当你调用commit()的时候, FragmentManger会检查是否已经存储了它自己的状态, 如果已经存了, 就抛出IllegalStateException。
那么如果你调用的是commitAllowingStateLoss(),则FragmentManger不会检查是否已经存储了它自己的状态(上面代码中已添加备注说明)，并且要是在onSaveInstanceState()之后,你可能会丢失掉什么状态呢? 答案是你可能会丢掉FragmentManager的状态, 即save之后任何被添加或被移除的Fragments.

**commit(), commitNow() 和 executePendingTransactions()**
> 使用commit()的时候, 一旦调用, 这个commit并不是立即执行的, 它会被发送到主线程的任务队列当中去, 当主线程准备好执行它的时候执行.
popBackStack()的工作也是这样, 发送到主线程任务队列中去. 也即说它们都是异步的.但是有时候你希望你的操作是立即执行的,之前的开发者会在commit()调用之后加上 executePendingTransactions()来保证立即执行, 即变异步为同步.support library从v24.0.0开始提供了 commitNow()方法,之前用executePendingTransactions()会将所有pending在队列中还有你新提交的transactions都执行了, 而commitNow()将只会执行你当前要提交的transaction. 所以commitNow()避免你会不小心执行了那些你可能并不想执行的transactions.

> 但是你不能对要加在back stack中的transaction使用commitNow(),即addToBackStack()和commitNow()不能同时使用.为什么呢?
想想一下, 如果你有一个提交使用了commit(), 紧接着又有另一个提交使用了commitNow(), 两个都想加入back stack, 那back stack会变成什么样呢? 到底是哪个transaction在上, 哪个在下? 答案将是一种不确定的状态, 因为系统并没有提供任何保证来确保顺序, 所以系统决定干脆不支持这个操作.前面提过popBackStack()是异步的, 所以它同样也有一个同步的兄弟popBackStackImmediate().所以实际应用的时候怎么选择呢? 1.如果你需要同步的操作, 并且你不需要加到back stack里, 使用commitNow().
support library在FragmentPagerAdapter里就使用了commitNow()来保证在更新结束的时候, 正确的页面被加上或移除.
2.如果你操作很多transactions, 并且不需要同步, 或者你需要把transactions加在back stack里, 那就使用commit().
3.如果你希望在某一个指定的点, 确保所有的transactions都被执行, 那么使用executePendingTransactions().

好啦，我们可以继续分析接下的啦（mExecCommit（Runnable）的run方法）：
```
// FragmentManagerImpl类：
    Runnable mExecCommit = new Runnable() {
        public void run() {
            FragmentManagerImpl.this.execPendingActions();
        }
    };
    
    public boolean execPendingActions() {
        this.ensureExecReady(true);

        boolean didSomething;
        // mTmpRecords：临时存储所有待执行的动作(mPendingActions)生成的 BackStackRecord
        // mTmpIsPop：存储 BackStackRecord 是否为出栈。
        for(didSomething = false; this.generateOpsForPendingActions(this.mTmpRecords, this.mTmpIsPop); didSomething = true) {
            this.mExecutingActions = true;

            try {
                this.removeRedundantOperationsAndExecute(this.mTmpRecords, this.mTmpIsPop);
            } finally {
                this.cleanupExec();
            }
        }

        this.doPendingDeferredStart();
        this.burpActive();
        return didSomething;
    }
    
    // 遍历 mPendingActions 调用 OpGenerator.generateOps() 方法生成 BackStackRecord 添加到 mTmpRecords 并把是否为出栈添加到 mTmpIsPop 中
    private boolean generateOpsForPendingActions(ArrayList<BackStackRecord> records, ArrayList<Boolean> isPop) {
        boolean didSomething = false;
        synchronized(this) {
            if (this.mPendingActions != null && this.mPendingActions.size() != 0) {
                int numActions = this.mPendingActions.size();

                for(int i = 0; i < numActions; ++i) {
                    didSomething |= ((FragmentManagerImpl.OpGenerator)this.mPendingActions.get(i)).generateOps(records, isPop);
                }
                // 清空待定任务队列
                this.mPendingActions.clear();
                this.mHost.getHandler().removeCallbacks(this.mExecCommit);
                return didSomething;
            } else {
                return false;
            }
        }
    }
    
// BackStackRecord类
    public boolean generateOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Run: " + this);
        }

        records.add(this);
        // 添加false
        isRecordPop.add(false);
        if (this.mAddToBackStack) {
            // 添加到“回退栈”中
            this.mManager.addBackStackState(this);
        }

        return true;
    }
    
// FragmentManagerImpl类的内部类PopBackStackState：
    private class PopBackStackState implements FragmentManagerImpl.OpGenerator {
        ...
        public boolean generateOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
            ...
            return FragmentManagerImpl.this.popBackStackState(records, isRecordPop, this.mName, this.mId, this.mFlags);
        }
    }
// FragmentManagerImpl类:
    boolean popBackStackState(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, String name, int id, int flags) {
        if (this.mBackStack == null) {
            return false;
        } else {
            int index;
            if (name == null && id < 0 && (flags & 1) == 0) {
                ...
                records.add(this.mBackStack.remove(index));
                // 添加true
                isRecordPop.add(true);
            } else {
                ...
                for(int i = this.mBackStack.size() - 1; i > index; --i) {
                    records.add(this.mBackStack.remove(i));
                    // 添加true
                    isRecordPop.add(true);
                }
            }

            return true;
        }
    }
    
    void addBackStackState(BackStackRecord state) {
        if (this.mBackStack == null) {
            this.mBackStack = new ArrayList();
        }
        // “回退栈” == mBackStack（ArrayList<BackStackRecord>）
        this.mBackStack.add(state);
    }
```
在 Runnable 中执行 execPendingActions() 方法，该方法分为几点来分析:
1. 通过generateOpsForPendingActions方法遍历 mPendingActions 调用 OpGenerator.generateOps() 方法设置了 mTmpRecords(临时存储所有待执行的动作:BackStackRecord) 和 mTmpIsPop (存储 BackStackRecord 是否为出栈)
2. OpGenerator.generateOps()方法，上面曾提起过OpGenerator接口会有两个地方实现，而BackStackRecord类实现OpGenerator接口中的generateOps()方法上面代码给出了源码，其实就是设置this自身添加到records(即mTmpRecords)集合中，并同时添加一个false到isRecordPop(即mTmpIsPop)集合里表示此动作不是“回退栈”的出栈操作。而另一个实现OpGenerator接口的generateOps()方法里isRecordPop(即mTmpIsPop)集合内添加的是true（源码已展示）




![](https://user-gold-cdn.xitu.io/2019/6/1/16b0ef321477bf90?w=267&h=245&f=png&s=52416)
我们接着往下走removeRedundantOperationsAndExecute()：
```
// FragmentManagerImpl类:
    private void removeRedundantOperationsAndExecute(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        if (records != null && !records.isEmpty()) {
            if (isRecordPop != null && records.size() == isRecordPop.size()) {
                this.executePostponedTransaction(records, isRecordPop);
                int numRecords = records.size();
                int startIndex = 0;

                for(int recordNum = 0; recordNum < numRecords; ++recordNum) {
                    boolean canReorder = ((BackStackRecord)records.get(recordNum)).mReorderingAllowed;
                    if (!canReorder) {
                        if (startIndex != recordNum) {
                            this.executeOpsTogether(records, isRecordPop, startIndex, recordNum);
                        }

                        int reorderingEnd = recordNum + 1;
                        // 根据上面的分析，只有“回退栈”执行出栈才会执行此处代码
                        if ((Boolean)isRecordPop.get(recordNum)) {
                            while(reorderingEnd < numRecords && (Boolean)isRecordPop.get(reorderingEnd) && !((BackStackRecord)records.get(reorderingEnd)).mReorderingAllowed) {
                                ++reorderingEnd;
                            }
                        }

                        this.executeOpsTogether(records, isRecordPop, recordNum, reorderingEnd);
                        startIndex = reorderingEnd;
                        recordNum = reorderingEnd - 1;
                    }
                }

                if (startIndex != numRecords) {
                    this.executeOpsTogether(records, isRecordPop, startIndex, numRecords);
                }

            } else {
                throw new IllegalStateException("Internal error with the back stack records");
            }
        }
    }
    
    private void executeOpsTogether(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        boolean allowReordering = ((BackStackRecord)records.get(startIndex)).mReorderingAllowed;
        boolean addToBackStack = false;
        if (this.mTmpAddedFragments == null) {
            this.mTmpAddedFragments = new ArrayList();
        } else {
            this.mTmpAddedFragments.clear();
        }

        this.mTmpAddedFragments.addAll(this.mAdded);
        Fragment oldPrimaryNav = this.getPrimaryNavigationFragment();

        int postponeIndex;
        for(postponeIndex = startIndex; postponeIndex < endIndex; ++postponeIndex) {
            BackStackRecord record = (BackStackRecord)records.get(postponeIndex);
            boolean isPop = (Boolean)isRecordPop.get(postponeIndex);
            // 对mOps进行优化，add或者remove（mOps即第三部分提到的保存add进的fragmnet集合）
            if (!isPop) {
                // 在28之前这里会执行 expandReplaceOps 方法把 replace 替换(目标 fragment 已经被 add )成相应的 remove 和 add 两个操作，或者(目标 fragment 没有被 add )只替换成 add 操作。
                oldPrimaryNav = record.expandOps(this.mTmpAddedFragments, oldPrimaryNav);
            } else {
                oldPrimaryNav = record.trackAddedFragmentsInPop(this.mTmpAddedFragments, oldPrimaryNav);
            }

            addToBackStack = addToBackStack || record.mAddToBackStack;
        }

        this.mTmpAddedFragments.clear();
        if (!allowReordering) {
            FragmentTransition.startTransitions(this, records, isRecordPop, startIndex, endIndex, false);
        }
        // 如果allowReordering为true，则此方法后面会走不通，则会走下面的判断，最终都会到达我们这部分最重要的方法————moveToState
        executeOps(records, isRecordPop, startIndex, endIndex);
        postponeIndex = endIndex;
        if (allowReordering) {
        /**
         * allowReordering为true，走此逻辑
         *  允许重新排序(需要自己调用FragmentTransaction.setReorderingAllowed()方法设置)
         *  必须启用fragment事务中的重新排序(即allowReordering)，才能
         *  使延迟的fragment过渡生效,具体用法请参考：https://www.jianshu.com/p/232073710172
        */
            ArraySet<Fragment> addedFragments = new ArraySet();
            this.addAddedFragments(addedFragments);
            // 此方法最后会走到moveToState方法，具体源码请自行查看
            postponeIndex = this.postponePostponableTransactions(records, isRecordPop, startIndex, endIndex, addedFragments);
            this.makeRemovedFragmentsInvisible(addedFragments);
        }
        ...
    }
    
    private static void executeOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        for(int i = startIndex; i < endIndex; ++i) {
            BackStackRecord record = (BackStackRecord)records.get(i);
            boolean isPop = (Boolean)isRecordPop.get(i);
            // 从上面分析可知isRecordPop取出来的是false（“回退栈”出栈时为true）
            if (isPop) {
                record.bumpBackStackNesting(-1);
                boolean moveToState = i == endIndex - 1;
                // 若为回退栈出栈操作，则执行此方法，
                // 此方法中根据op.cmd判断对framgnet进行相应的处理，
                // 与else分支相同的cmd指令处理逻辑不同
                record.executePopOps(moveToState);
            } else {
                record.bumpBackStackNesting(1);
                record.executeOps();
            }
        }
    }
```
我们从removeRedundantOperationsAndExecute()方法的源码中可以看到该方法实际上是对records集合中所有动作的startIndex(起始动作位置), recordNum(需要操作的动作个数)的设置，然后都会去调用executeOpsTogether()方法，而executeOpsTogether()方法我们只展示部分代码，其中会对mOps进行扩展操作，最后调用方法executeOps()继续操作，而方法executeOps中又经过判断最终调用BackStackRecord类的executeOps()方法。
```
// BackStackRecord类:
    void executeOps() {
        int numOps = this.mOps.size();
        // 遍历执行所有的mOps（包含我们commit操作前的所有其它操作，比如：add、hide等）
        for(int opNum = 0; opNum < numOps; ++opNum) {
            BackStackRecord.Op op = (BackStackRecord.Op)this.mOps.get(opNum);
            Fragment f = op.fragment;
            if (f != null) {
                f.setNextTransition(this.mTransition, this.mTransitionStyle);
            }
            // 根据op.cmd指令进行操作，相信根据下面每个分支的逻辑能分别出每个指令对应的操作
            switch(op.cmd) {
            case 1:
                f.setNextAnim(op.enterAnim);
                this.mManager.addFragment(f, false);
                break;
            case 2:
            default:
                throw new IllegalArgumentException("Unknown cmd: " + op.cmd);
            case 3:
                f.setNextAnim(op.exitAnim);
                this.mManager.removeFragment(f);
                break;
            case 4:
                f.setNextAnim(op.exitAnim);
                this.mManager.hideFragment(f);
                break;
            case 5:
                f.setNextAnim(op.enterAnim);
                this.mManager.showFragment(f);
                break;
            case 6:
                f.setNextAnim(op.exitAnim);
                this.mManager.detachFragment(f);
                break;
            case 7:
                f.setNextAnim(op.enterAnim);
                this.mManager.attachFragment(f);
                break;
            case 8:
                this.mManager.setPrimaryNavigationFragment(f);
                break;
            case 9:
                this.mManager.setPrimaryNavigationFragment((Fragment)null);
            }

            if (!this.mReorderingAllowed && op.cmd != 1 && f != null) {
                this.mManager.moveFragmentToExpectedState(f);
            }
        }
        // 只有没设置setReorderingAllowed(true)的才能继续，
        // 而设置的会在前面的某步逻辑当中走到moveToState方法内，上面有说明
        if (!this.mReorderingAllowed) {
            // 最后调用我们这部分最重要的方法：moveToState
            this.mManager.moveToState(this.mManager.mCurState, true);
        }
    }
 
 // 1. 提交add操作时将当前提交的fragmen添加进mActive和mAdded里
    // 并重置fragment.mAdded和fragment.mRemoving两个的状态
    public void addFragment(Fragment fragment, boolean moveToStateNow) {
        if (DEBUG) {
            Log.v("FragmentManager", "add: " + fragment);
        }
        // 调用makeActive，将fragment添加进mActive
        this.makeActive(fragment);
        if (!fragment.mDetached) {
            if (this.mAdded.contains(fragment)) {
                throw new IllegalStateException("Fragment already added: " + fragment);
            }

            synchronized(this.mAdded) {
                // 将fragment也添加进mAdded里
                this.mAdded.add(fragment);
            }
            // 设置mAdded和mRemoving状态
            fragment.mAdded = true;
            fragment.mRemoving = false;
            if (fragment.mView == null) {
                fragment.mHiddenChanged = false;
            }

            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }

            if (moveToStateNow) {
                this.moveToState(fragment);
            }
        }
    }
    void makeActive(Fragment f) {
        if (f.mIndex < 0) {
            f.setIndex(this.mNextFragmentIndex++, this.mParent);
            if (this.mActive == null) {
                this.mActive = new SparseArray();
            }
            // 将fragment添加进mActive
            this.mActive.put(f.mIndex, f);
            if (DEBUG) {
                Log.v("FragmentManager", "Allocated fragment index " + f);
            }
        }
    }

// 2. 提交remove操作时将当前提交的fragment从mAdded移除
    // 并重置fragment.mAdded和fragment.mRemoving两个的状态
    public void removeFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v("FragmentManager", "remove: " + fragment + " nesting=" + fragment.mBackStackNesting);
        }

        boolean inactive = !fragment.isInBackStack();
        if (!fragment.mDetached || inactive) {
            synchronized(this.mAdded) {
                this.mAdded.remove(fragment);
            }

            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }

            fragment.mAdded = false;
            fragment.mRemoving = true;
        }

    }
    
// 3. 设置fragment.mHidden的状态为false
    public void showFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v("FragmentManager", "show: " + fragment);
        }

        if (fragment.mHidden) {
            fragment.mHidden = false;
            fragment.mHiddenChanged = !fragment.mHiddenChanged;
        }

    }
    
// 4. 设置fragment.mHidden的状态为true
    public void hideFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v("FragmentManager", "hide: " + fragment);
        }

        if (!fragment.mHidden) {
            fragment.mHidden = true;
            fragment.mHiddenChanged = !fragment.mHiddenChanged;
        }

    }
```
哈哈，这段代码似乎有点多，不过好理解，我们来先说一下这段代码里都有些什么，我们从executeOps()方法中可以看到该方法里通过for循环对mOps进行了遍历，而此次遍历会对我们本次commit提交的所有操作进行设置。比如我们上面列出的四中操作：add、remove、show和hide一样。

我们先来看这段代码中的两个集合：**mAdded和mActive**

1. mAdded：包含了所有已经 added 并且没有被从Activity中removed和detached的Fragments

    注：如果一个 Fragment被添加到Activity中那么这个Fragment会被added到该列表。Fragment被从Activity中removed或者Fragment从Activity中detached，则就会被从该列表中移除。

2. mAdded 的一个超集，是绑定到一个 Activity 上的所有 Fragment。包括返回栈中所有的通过任何 FragmentTransaction 添加的 Fragments。这是非常重要的因为如下原因:
* 当一个 Activity 要保存它的 State 时，它必须保存它所有 Fragment 的状态，因为 mActive 保存了所有 Fragment，所以系统只要存储这个列表里的 Fragment 的状态就好了。而mAdded 只是被序列化成一个整形数组，每个元素指向 Fragment 在 mActive 中的下标位置(这块在前面 Fragment 的存储与恢复中分析到了)。
* 在恢复 Activity 的状态时，FragmentManager 的状态也会被恢复，mActive 列表就可以被用来恢复 mAdded 列表，因为保存状态的时候mAdded 被简单的保存为整形数组。

* 当一个 Activity 经历它的各生命周期时，它必须引起所有绑定的 Fragment 经历各自的生命周期。

    * 该 Activity 的 FragmentManager 有义务去引导所有 Fragemnt 转换到正确的状态，这其中包括屏幕上可见的 Fragment 的 View 层级的初始化，并且调用正确的生命周期函数。
    * 为了确保完整，FragmentManager 将遍历mActive 中所有的 Fragment，而不仅仅是 mAdded。
* 它持有所有 BackStack 返回栈引用的对象。

    *  这确保了返回栈中对 Fragment 操作的回滚能够实现。

    注：如果一个Fragment被添加到Activity中那么这个Fragment会被added到该列表。只有在两种情况 Fragment才会被从该列表中移除:一是，Fragment被从Activity中移除并且没有在返回栈中；二是一个transaction从返回栈中被pop出来、Fragment的add或者replace操作被逆向，即返回栈不再持有 Fragment。

我们接着来说moveToState这个方法：
```
// FragmentManagerImpl类：
    void moveToState(int newState, boolean always) {
        if (this.mHost == null && newState != 0) {
            throw new IllegalStateException("No activity");
        } else if (always || newState != this.mCurState) {
            this.mCurState = newState;
            if (this.mActive != null) {
                int numAdded = this.mAdded.size();
                
                int numActive;
                // 遍历mAdded集合，肯定会走此代码逻辑
                for(numActive = 0; numActive < numAdded; ++numActive) {
                    Fragment f = (Fragment)this.mAdded.get(numActive);
                    // 将fragment移至预期状态
                    this.moveFragmentToExpectedState(f);
                }

                numActive = this.mActive.size();
                // // 遍历mActive集合（若调用回退栈出栈，则会走此出代码）
                for(int i = 0; i < numActive; ++i) {
                    Fragment f = (Fragment)this.mActive.valueAt(i);
                    // 当前framgnet不为空 并且 此时操作为remove或者detach 并且不是新添加的则会执行下面代码
                    if (f != null && (f.mRemoving || f.mDetached) && !f.mIsNewlyAdded) {
                        // // 将fragment移至预期状态
                        this.moveFragmentToExpectedState(f);
                    }
                }
                // fragment的成员属性f.mDeferStart为true才能走通下面代码(具体暂不分析)
                this.startPendingDeferredFragments();
                if (this.mNeedMenuInvalidate && this.mHost != null && this.mCurState == 4) {
                    this.mHost.onSupportInvalidateOptionsMenu();
                    this.mNeedMenuInvalidate = false;
                }
            }
        }
    }
```
我们来看一下moveToState方法中的参数newState，BackStackRecord类中的executeOps里传过来的是mManager.mCurState，而mManager.mCurState默认为0，即需要add的状态。我们现在回想一下fragment为什么会随着Activity的周期变化而变化呢？我们来看段代码：
```
// FragmentActivity类：
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.mFragments.attachHost((Fragment)null);
        super.onCreate(savedInstanceState);
        ......
        this.mFragments.dispatchCreate();
    }

// FragmentController类：
    public void dispatchCreate() {
        this.mHost.mFragmentManager.dispatchCreate();
    }
    
// FragmentManagerImpl类：
    public void dispatchCreate() {
        this.mStateSaved = false;
        this.mStopped = false;
        this.dispatchStateChange(1);
    }
    private void dispatchStateChange(int nextState) {
        try {
            this.mExecutingActions = true;
            // 这里走到了moveToState
            this.moveToState(nextState, false);
        } finally {
            this.mExecutingActions = false;
        }

        this.execPendingActions();
    }
```
这段代码是从上到下的顺序执行的，我们可以看到在FragmentActivity的onCreate周期方法中一步一步的走到了moveToState，而moveToState正是走到fragment周期方法的关键（FragmentActivity的其它周期方法同onCreate方法也会走到对应的fragment的周期方法中）。既然moveToState是走到fragment周期方法的关键，那我们继续往下分析，上面已经说到会走到FragmentManagerImpl类的moveFragmentToExpectedState(f)方法中：
```
// FragmentManagerImpl类:
    void moveFragmentToExpectedState(Fragment f) {
        if (f != null) {
            int nextState = this.mCurState;
            if (f.mRemoving) {
                // 如果操作为remove则nextState设置为1或者0，用于后面判断
                if (f.isInBackStack()) {
                    nextState = Math.min(nextState, 1);
                } else {
                    nextState = Math.min(nextState, 0);
                }
            }
            // 继续走同名方法
            this.moveToState(f, nextState, f.getNextTransition(), f.getNextTransitionStyle(), false);
            // 如果当前View不为空，则添加布局执行动画
            if (f.mView != null) {
                Fragment underFragment = this.findFragmentUnder(f);
                if (underFragment != null) {
                    View underView = underFragment.mView;
                    ViewGroup container = f.mContainer;
                    int underIndex = container.indexOfChild(underView);
                    int viewIndex = container.indexOfChild(f.mView);
                    if (viewIndex < underIndex) {
                        container.removeViewAt(viewIndex);
                        container.addView(f.mView, underIndex);
                    }
                }

                if (f.mIsNewlyAdded && f.mContainer != null) {
                    if (f.mPostponedAlpha > 0.0F) {
                        f.mView.setAlpha(f.mPostponedAlpha);
                    }

                    f.mPostponedAlpha = 0.0F;
                    f.mIsNewlyAdded = false;
                    FragmentManagerImpl.AnimationOrAnimator anim = this.loadAnimation(f, f.getNextTransition(), true, f.getNextTransitionStyle());
                    if (anim != null) {
                        setHWLayerAnimListenerIfAlpha(f.mView, anim);
                        if (anim.animation != null) {
                            f.mView.startAnimation(anim.animation);
                        } else {
                            anim.animator.setTarget(f.mView);
                            anim.animator.start();
                        }
                    }
                }
            }

            if (f.mHiddenChanged) {
                // 完成显示隐藏fragment
                this.completeShowHideFragment(f);
            }

        }
    }
    
    void moveToState(Fragment f, int newState, int transit, int transitionStyle, boolean keepActive) {
        // 重新判断设置newState状态值
        if ((!f.mAdded || f.mDetached) && newState > 1) {
            newState = 1;
        }

        if (f.mRemoving && newState > f.mState) {
            if (f.mState == 0 && f.isInBackStack()) {
                newState = 1;
            } else {
                newState = f.mState;
            }
        }

        if (f.mDeferStart && f.mState < 3 && newState > 2) {
            newState = 2;
        }
        
        // 如果fragment自身的状态<=newState状态，则证明此时fragment是被创建阶段
        if (f.mState <= newState) {
            label297: {
                if (f.mFromLayout && !f.mInLayout) {
                    return;
                }

                if (f.getAnimatingAway() != null || f.getAnimator() != null) {
                    f.setAnimatingAway((View)null);
                    f.setAnimator((Animator)null);
                    this.moveToState(f, f.getStateAfterAnimating(), 0, 0, true);
                }
                // 判断fragment状态进行处理
                switch(f.mState) {
                case 0:
                    if (newState > 0) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "moveto CREATED: " + f);
                        }

                        if (f.mSavedFragmentState != null) {
                            f.mSavedFragmentState.setClassLoader(this.mHost.getContext().getClassLoader());
                            f.mSavedViewState = f.mSavedFragmentState.getSparseParcelableArray("android:view_state");
                            f.mTarget = this.getFragment(f.mSavedFragmentState, "android:target_state");
                            if (f.mTarget != null) {
                                f.mTargetRequestCode = f.mSavedFragmentState.getInt("android:target_req_state", 0);
                            }

                            if (f.mSavedUserVisibleHint != null) {
                                f.mUserVisibleHint = f.mSavedUserVisibleHint;
                                f.mSavedUserVisibleHint = null;
                            } else {
                                f.mUserVisibleHint = f.mSavedFragmentState.getBoolean("android:user_visible_hint", true);
                            }

                            if (!f.mUserVisibleHint) {
                                f.mDeferStart = true;
                                if (newState > 2) {
                                    newState = 2;
                                }
                            }
                        }

                        f.mHost = this.mHost;
                        f.mParentFragment = this.mParent;
                        f.mFragmentManager = this.mParent != null ? this.mParent.mChildFragmentManager : this.mHost.getFragmentManagerImpl();
                        if (f.mTarget != null) {
                            if (this.mActive.get(f.mTarget.mIndex) != f.mTarget) {
                                throw new IllegalStateException("Fragment " + f + " declared target fragment " + f.mTarget + " that does not belong to this FragmentManager!");
                            }

                            if (f.mTarget.mState < 1) {
                                this.moveToState(f.mTarget, 1, 0, 0, true);
                            }
                        }

                        this.dispatchOnFragmentPreAttached(f, this.mHost.getContext(), false);
                        f.mCalled = false;
                        // 执行fragment的onAttach周期方法
                        f.onAttach(this.mHost.getContext());
                        if (!f.mCalled) {
                            throw new SuperNotCalledException("Fragment " + f + " did not call through to super.onAttach()");
                        }

                        if (f.mParentFragment == null) {
                            this.mHost.onAttachFragment(f);
                        } else {
                            f.mParentFragment.onAttachFragment(f);
                        }

                        this.dispatchOnFragmentAttached(f, this.mHost.getContext(), false);
                        if (!f.mIsCreated) {
                            this.dispatchOnFragmentPreCreated(f, f.mSavedFragmentState, false);
                            f.performCreate(f.mSavedFragmentState);
                            this.dispatchOnFragmentCreated(f, f.mSavedFragmentState, false);
                        } else {
                            f.restoreChildFragmentState(f.mSavedFragmentState);
                            f.mState = 1;
                        }

                        f.mRetaining = false;
                    }
                case 1:
                    this.ensureInflatedFragmentView(f);
                    if (newState > 1) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "moveto ACTIVITY_CREATED: " + f);
                        }

                        if (!f.mFromLayout) {
                            ViewGroup container = null;
                            if (f.mContainerId != 0) {
                                if (f.mContainerId == -1) {
                                    this.throwException(new IllegalArgumentException("Cannot create fragment " + f + " for a container view with no id"));
                                }

                                container = (ViewGroup)this.mContainer.onFindViewById(f.mContainerId);
                                if (container == null && !f.mRestored) {
                                    String resName;
                                    try {
                                        resName = f.getResources().getResourceName(f.mContainerId);
                                    } catch (NotFoundException var9) {
                                        resName = "unknown";
                                    }

                                    this.throwException(new IllegalArgumentException("No view found for id 0x" + Integer.toHexString(f.mContainerId) + " (" + resName + ") for fragment " + f));
                                }
                            }

                            f.mContainer = container;
                           // 执行fragment的onCreateView周期方法 f.performCreateView(f.performGetLayoutInflater(f.mSavedFragmentState), container, f.mSavedFragmentState);
                            if (f.mView == null) {
                                f.mInnerView = null;
                            } else {
                                f.mInnerView = f.mView;
                                f.mView.setSaveFromParentEnabled(false);
                                if (container != null) {
                                    container.addView(f.mView);
                                }

                                if (f.mHidden) {
                                    f.mView.setVisibility(8);
                                }
                                // 执行fragment的onViewCreated周期方法
                                f.onViewCreated(f.mView, f.mSavedFragmentState);
                                this.dispatchOnFragmentViewCreated(f, f.mView, f.mSavedFragmentState, false);
                                f.mIsNewlyAdded = f.mView.getVisibility() == 0 && f.mContainer != null;
                            }
                        }

                        f.performActivityCreated(f.mSavedFragmentState);
                        this.dispatchOnFragmentActivityCreated(f, f.mSavedFragmentState, false);
                        if (f.mView != null) {
                            f.restoreViewState(f.mSavedFragmentState);
                        }
                        // 执行fragment的onActivityCreated周期方法
                        f.mSavedFragmentState = null;
                    }
                case 2:
                    if (newState > 2) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "moveto STARTED: " + f);
                        }
                        // 执行framgnet的onStart周期方法
                        f.performStart();
                        this.dispatchOnFragmentStarted(f, false);
                    }
                case 3:
                    break;
                default:
                    break label297;
                }

                if (newState > 3) {
                    if (DEBUG) {
                        Log.v("FragmentManager", "moveto RESUMED: " + f);
                    }
                    // 执行framgnet的onResume周期方法
                    f.performResume();
                    this.dispatchOnFragmentResumed(f, false);
                    f.mSavedFragmentState = null;
                    f.mSavedViewState = null;
                }
            }
        // 如果fragment自身的状态>=newState状态，则证明此时fragment是被销毁阶段
        } else if (f.mState > newState) {
            switch(f.mState) {
            case 4:
                if (newState < 4) {
                    if (DEBUG) {
                        Log.v("FragmentManager", "movefrom RESUMED: " + f);
                    }
                    // 执行framgnet的onPause周期方法
                    f.performPause();
                    this.dispatchOnFragmentPaused(f, false);
                }
            case 3:
                if (newState < 3) {
                    if (DEBUG) {
                        Log.v("FragmentManager", "movefrom STARTED: " + f);
                    }
                    // 执行framgnet的onStop周期方法
                    f.performStop();
                    this.dispatchOnFragmentStopped(f, false);
                }
            case 2:
                if (newState < 2) {
                    if (DEBUG) {
                        Log.v("FragmentManager", "movefrom ACTIVITY_CREATED: " + f);
                    }

                    if (f.mView != null && this.mHost.onShouldSaveFragmentState(f) && f.mSavedViewState == null) {
                        this.saveFragmentViewState(f);
                    }
                    // 执行framgnet的onDestroyView周期方法
                    f.performDestroyView();
                    this.dispatchOnFragmentViewDestroyed(f, false);
                    if (f.mView != null && f.mContainer != null) {
                        f.mContainer.endViewTransition(f.mView);
                        f.mView.clearAnimation();
                        FragmentManagerImpl.AnimationOrAnimator anim = null;
                        if (this.mCurState > 0 && !this.mDestroyed && f.mView.getVisibility() == 0 && f.mPostponedAlpha >= 0.0F) {
                            anim = this.loadAnimation(f, transit, false, transitionStyle);
                        }

                        f.mPostponedAlpha = 0.0F;
                        if (anim != null) {
                            this.animateRemoveFragment(f, anim, newState);
                        }

                        f.mContainer.removeView(f.mView);
                    }

                    f.mContainer = null;
                    f.mView = null;
                    f.mViewLifecycleOwner = null;
                    f.mViewLifecycleOwnerLiveData.setValue((Object)null);
                    f.mInnerView = null;
                    f.mInLayout = false;
                }
            case 1:
                if (newState < 1) {
                    if (this.mDestroyed) {
                        if (f.getAnimatingAway() != null) {
                            View v = f.getAnimatingAway();
                            f.setAnimatingAway((View)null);
                            v.clearAnimation();
                        } else if (f.getAnimator() != null) {
                            Animator animator = f.getAnimator();
                            f.setAnimator((Animator)null);
                            animator.cancel();
                        }
                    }

                    if (f.getAnimatingAway() == null && f.getAnimator() == null) {
                        if (DEBUG) {
                            Log.v("FragmentManager", "movefrom CREATED: " + f);
                        }

                        if (!f.mRetaining) {
                            // 执行framgnet的onDestroy周期方法
                            f.performDestroy();
                            this.dispatchOnFragmentDestroyed(f, false);
                        } else {
                            f.mState = 0;
                        }
                        // 执行framgnet的onDetach周期方法
                        f.performDetach();
                        this.dispatchOnFragmentDetached(f, false);
                        if (!keepActive) {
                            if (!f.mRetaining) {
                                this.makeInactive(f);
                            } else {
                                f.mHost = null;
                                f.mParentFragment = null;
                                f.mFragmentManager = null;
                            }
                        }
                    } else {
                        f.setStateAfterAnimating(newState);
                        newState = 1;
                    }
                }
            }
        }

        if (f.mState != newState) {
            Log.w("FragmentManager", "moveToState: Fragment state for " + f + " not updated inline; " + "expected state " + newState + " found " + f.mState);
            f.mState = newState;
        }

    }
```
继续从FragmentManagerImpl类的moveFragmentToExpectedState(f)方法中说起，该方法中又会继续调用moveToState方法，这个方法和上面的moveToState方法不同，这俩方法是同名不同参的方法，该方法中会根据fragment的mState自身的状态值和newState传过来的状态值进行比较来区分：当前fragment是走创建阶段的周期方法还是销毁阶段的周期方法，进一步再通过fragment的mState判断到底要走哪个fragment的周期方法，具体标注可看代码注释哦。

Fragment的7种状态（mState）：

* static final int INVALID_STATE = -1;   // 作为null值的非法状态
* static final int INITIALIZING = 0;     // 没有被create
* static final int CREATED = 1;          // 已经create
* static final int ACTIVITY_CREATED = 2; // Activity已经完成了create
* static final int STOPPED = 3;          // 完全创建，还没start
* static final int STARTED = 4;          // 已经create和start，还没有resume
* static final int RESUMED = 5;          // 已经完成create,start和resume

至此我们的Fragment的源码分析按照一开始的流程已分析完毕。

## “回退栈”：BackStackRecord 出栈
我们最后说一下回退栈（FragmentManagerImpl的成员mBackStack），其实我们在cooimt操作时我们就已经设置了“回退栈”内的元素。重新看一下该部分代码：
```
// FragmentManagerImpl类：
    public boolean generateOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        if (FragmentManagerImpl.DEBUG) {
            Log.v("FragmentManager", "Run: " + this);
        }

        records.add(this);
        isRecordPop.add(false);
        if (this.mAddToBackStack) {
            this.mManager.addBackStackState(this);
        }

        return true;
    }
    void addBackStackState(BackStackRecord state) {
        if (this.mBackStack == null) {
            this.mBackStack = new ArrayList();
        }

        this.mBackStack.add(state);
    }
```
我们回顾一下上面的逻辑，在执行BackStackRecord类(实现了OpGenerator接口)的方法generateOps时，就已经将当前的BackStackRecord入栈啦。而BackStackRecord出栈主要是调用如下几个方法：

* popBackStack()
* popBackStackImmediate()
* popBackStack(int id/String name, int flags)
* popBackStackImmediate(int id/String name, int flags)

PopBackStackState类 实现了 OpGenerator 接口,具体实现如下：

* 参数 records 用来存放出栈的 BackStackRecord
* 参数 isRecordPop 用来存放相应 BackStackRecord 是否为出栈(显然为 true)
* 参数 name 表示出栈到相应 name 的 BackStackRecord
* 参数 id 表示出栈到相应 id 的 BackStackRecord
* 参数 flags (0 或者 POP_BACK_STACK_INCLUSIVE)
* POP_BACK_STACK_INCLUSIVE 如果参数 flags ==POP_BACK_STACK_INCLUSIVE 并且设置了 name 或者 id 那么，所有符合该 name 或者 id 的 BackStackRecord 都将被匹配，直到遇到一个不匹配的或者到达了栈底，然后出栈所有 BackStackRecord 直到最终匹配到的下标位置。否则只匹配第一次 name 或者 id 相符的 BackStackRecord，然后出栈所有 BackStackRecord 直到但不包括匹配到的下标位置。

若我们自己主动调用popBackStack两个方法之一，实际上就是调用了enqueueAction方法，并传入PopBackStackState类的新创建实例，而此时isRecordPop集合里存的值就是true。在上面部分分析中也说明了isRecordPop集合中存的元素对代码逻辑的影响。最后会导致界面显示的是上一个fragment视图。

如果 回退栈 mBackStack 为空就终止出栈操作并返回 false，当name == null && id < 0 && (flags & POP_BACK_STACK_INCLUSIVE) == 0 (调用的是popBackStack()方法)时，把返回栈最后一个 BackStackRecord出栈。当 name 或者 id 被指定的时候，倒序遍历 mBackStack ，如果遇到 name 或者 id 相符就退出循环，此时 index 为第一次匹配到的下标，如果flags==POP_BACK_STACK_INCLUSIVE 继续遍历返回栈，直至栈底或者遇到不匹配的跳出循环。最后出栈所有 BackStackRecord。

## 总结(借用)：

1. show/hideFragment只是改变fragment根View的visibility，最多带上个动画效果，另外只有本身是hidden的fragment，调用show才起作用，否则没用的，fragment.onHiddenChanged会被触发；其次不会有生命周期callback触发，当然了这些操作的前提是已经被add了的fragment；
2. add Fragment的时候，不管加不加入回退栈都一样，经历的生命周期如下：onAttach、onCreate、onCreateView、onActivityCreate、onStart、onResume；
3. removeFragment的时候，经历的生命周期如下：onPause、onStop、onDestroyView，如果不加回退栈还会继续走onDestroy、onDetach；remove的时候不仅从mAdded中移除fragment，也从mActive中移除了
4. attach/detach Fragment的前提都是已经add了的fragment，其生命周期回调不受回退栈影响。attach的时候onCreateView、onActivityCreate、onStart、onResume会被调用；detach的时候onPause、onStop、onDestroyView会被调用，onDestroy、onDetach不会被调用；对应的fragment只是从mAdded中移除了；

![](https://user-gold-cdn.xitu.io/2019/6/1/16b0eef8c3470423?w=360&h=360&f=png&s=71693)

参考链接：

https://blog.csdn.net/feather_wch/article/details/79462351#Fragment_557

https://www.jianshu.com/p/47d20351b1ac

https://www.jianshu.com/p/1a5ecf1d93c8

https://www.cnblogs.com/mengdd/p/5827045.html

<font color="#ff0000">**注：若有什么地方阐述有误，敬请指正。**</font>
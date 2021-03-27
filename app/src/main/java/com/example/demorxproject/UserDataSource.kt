package com.example.demorxproject

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object UserDataSource {

    private var userCounter = 0
    private val userSource: BehaviorSubject<User> = BehaviorSubject.create()

    /**
     * Простое создание Single из существующего метода
     */
    fun getSingleUsers(count: Int): Single<List<User>> =
        Single.fromCallable { generateUsers(count) }
            .map { users ->
                users.map { it.additionalData = "${it.name} + ${it.surname}" }
                users
            }

    /**
     * Пример создания Observable "ручками". Тот случай когда нам нужно перевести наш императивный мир в реактивный
     * Подобным образом мы могли бы код на колбеках перевести в RX
     */
    fun observableCreate(): Observable<User>? {
        return Observable.create { emitter ->
            try {
                generateUsers(100).forEach { user -> emitter.onNext(user) }
                emitter.onComplete()
            } catch (e: Error) {
                emitter.onError(e)
            }
        }
    }

    /**
     * Пример работы с Observable
     * Так же здесь мы можем рассмотреть то как мы один источник данных трансфрмируем в другой в методе map
     */
    fun getUsersFromObservable(): Observable<User> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .map { second -> generateUser(second.toInt()) }
    }

    /**
     * Пример того, как мы можем использовать flatMap
     * В этом примере мы условно добавляем некоторую информацию нашему пользовалю
     */
    fun getUsersObservableWithFlatMap(count: Int): Observable<List<User>> =
        Observable.fromCallable { generateUsers(count) }
            .flatMapSingle { users -> addInfo(users) }

    /**
     * Пример того как мы можем переключаться между потоками. Здесь хорошо видно что и на каком потоке будет выполняться
     */
    fun fromInterval(): Observable<User> =
        Observable.interval(1, TimeUnit.SECONDS)
            .map { generateUser() }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { user ->
                Log.d("xxx", Thread.currentThread().name)
                user
            }
            .observeOn(AndroidSchedulers.mainThread())
            .map { user ->
                Log.d("xxx", Thread.currentThread().name)
                user
            }

    /**
     * Тут мы имитируем некоторый горячий источник данных в отдельном потоке
     */
    fun startHotEmitter() {
        var n = 0
        Thread {
            while (n < 100000) {
                userSource.onNext(generateUser(n))
                n++
            }
        }.start()
    }

    /**
     * Здесь мы подписываемяс на горячий истоник данных и можем выбрать разные стратегии и понаблюдать разное поведение
     */
    fun getUsersWithBackPressure(): @NonNull Flowable<User> {
        return userSource.toFlowable(BackpressureStrategy.DROP)
    }

    /**
     * Это имитация некоторого другого потока RX.
     * К примеру здесь может быть ваш запрос в базу данных или API
     */
    private fun addInfo(users: List<User>): Single<List<User>> {
        val list = users.map {
            it.additionalData = Random.nextInt(1000000).toString()
            it
        }
        return Single.fromCallable { list }
    }


    private fun generateUsers(count: Int): List<User> {
        val users: MutableList<User> = mutableListOf()
        for (i in 0..count) {
            val n = Random.nextInt(names.size - 1)
            val s = Random.nextInt(surnames.size - 1)
            users.add(
                User(
                    id = "$n $s",
                    name = names[n],
                    surname = surnames[s],
                    age = Random.nextInt(100),
                    queue = ++userCounter
                )
            )
        }

        return users
    }

    private fun generateUser(queue: Int? = null): User {
        val n = Random.nextInt(names.size - 1)
        val s = Random.nextInt(surnames.size - 1)
        return User(
            id = "$n $s",
            name = names[n],
            surname = surnames[s],
            age = Random.nextInt(100),
            queue = queue ?: ++userCounter
        )
    }

    private val names: List<String> = listOf(
        "Jhon",
        "Mike",
        "Bob",
        "Batman",
        "Vatman",
        "Joy",
        "Persi",
        "Luke",
        "Duke",
        "Ktulhu",
        "Linux"
    )

    private val surnames: List<String> = listOf(
        "Travolta",
        "Tyson",
        "Marley",
        "Supermenovich",
        "Bumagniy",
        "Three Latters",
        "Abugugagagu",
        "Skywalker",
        "Nukem",
        "Skovorodkin",
        "Torvald"
    )
}
package com.example.demorxproject

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.demorxproject.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subscribers.DisposableSubscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val usersAdapter: UsersAdapter by lazy { UsersAdapter() }
    private val mutableUsers: MutableList<User> = mutableListOf()
    private lateinit var textObservable: Observable<String>
    private lateinit var subscription: Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initSearch()
        getUsersWithBackPressure()
    }

    private fun initViews() {
        with(binding) {
            userRecycler.adapter = usersAdapter
            refreshButton.setOnClickListener { subscription.request(3) }

        }
    }

    /**
     * Здесь небольшой упрощенный пример того как мы могли бы реализовать свой поиск
     */
    private fun initSearch() = binding.inputView.addTextChangedListener { text ->
        textObservable = Observable.create { emitter ->
            emitter.onNext(text.toString())
        }
        textObservable
            .filter { inputText -> inputText.isNotEmpty() }
            .distinctUntilChanged()
            .subscribe(
                { inputText ->
                    val filteredUsers = mutableUsers.filter { user -> user.name.contains(inputText) || user.surname.contains(inputText) }
                    usersAdapter.update(filteredUsers)
                },
                { error -> showError(error) },
                { Snackbar.make(binding.root, "Observable Compleated", Snackbar.LENGTH_SHORT).show() }
            )
    }

    private fun getSingleUsers() {
        UserDataSource.getSingleUsers(100)
            .subscribe(
                { users ->
                    mutableUsers.addAll(users)
                    usersAdapter.update(mutableUsers)
                },
                { error -> showError(error) })
    }

    private fun getUsersFromObservable() {
        UserDataSource.getUsersFromObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { user -> usersAdapter.update(mutableUsers.apply { add(user) }) },
                { error -> showError(error) },
                { Snackbar.make(binding.root, "Observable Compleated", Snackbar.LENGTH_SHORT).show() }
            )
    }

    private fun getUsersObservableWithFlatMap(count: Int) {
        UserDataSource.getUsersObservableWithFlatMap(count)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { users ->
                    mutableUsers.addAll(users)
                    usersAdapter.update(mutableUsers)
                },
                { error -> showError(error) },
                { Snackbar.make(binding.root, "Observable Compleated", Snackbar.LENGTH_SHORT).show() }
            )
    }

    private fun getUsersWithBackPressure() {
        UserDataSource.startHotEmitter()

        UserDataSource.getUsersWithBackPressure()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSubscriber<User>() {
                override fun onStart() {
                    Log.d("xxx", "On Start")
                    request(3)
                }

                override fun onNext(user: User) {
                    Log.d("xxx", "On next with ${user.name}")
                    usersAdapter.update(mutableUsers.apply { add(user) })
                    Handler().postDelayed({ request(2) }, 2000)
                }

                override fun onError(t: Throwable?) {
                    Log.d("xxx", "Error message = ${t?.message}")
                }

                override fun onComplete() {
                    Log.d("xxx", "Complete")
                }
            })
    }

    private fun showError(error: Throwable?) {
        Snackbar.make(binding.root, "Something wrong. Error: $error", Snackbar.LENGTH_SHORT).show()
    }
}
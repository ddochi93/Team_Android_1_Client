package com.eroom.erooja.feature.search.search_detail_page

import android.annotation.SuppressLint
import com.eroom.domain.api.usecase.goal.GetSearchGoalUsecase
import com.eroom.domain.utils.addTo
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class SearchDetailPresenter(override var view: SearchDetailContract.View,
                            private val getsearchgoalusecase: GetSearchGoalUsecase
): SearchDetailContract.Presenter{

    private val compositeDisposable = CompositeDisposable()

    @SuppressLint("CheckResult")
    override fun getSearchJobInterest(interestId: Long?) {
        getsearchgoalusecase.getSearchJobInterest(interestId)
            .subscribe ({
                view.checkContentSize(it.content.size)
            },{
                Timber.i(it.localizedMessage)
            }) addTo compositeDisposable
    }

    @SuppressLint("CheckResult")
    override fun getSearchGoalTitle(title: String?) {
        getsearchgoalusecase.getSearchGoalTitle("TITLE", title)
            .subscribe({
                view.checkContentSize(it.content.size)

            },{
                Timber.i(it.localizedMessage)
            })
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}
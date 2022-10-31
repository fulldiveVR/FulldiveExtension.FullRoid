package com.swordfish.lemuroid.app.tv.games

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TVGamesFragment : VerticalGridSupportFragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var coverLoader: CoverLoader

    private val args: TVGamesFragmentArgs by navArgs()

    init {
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 4
        setGridPresenter(gridPresenter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = TVGamesViewModel.Factory(retrogradeDb)
        val gamesViewModel = ViewModelProvider(this, factory)[TVGamesViewModel::class.java]

        val cardSize = resources.getDimensionPixelSize(R.dimen.card_size)
        val pagingAdapter = PagingDataAdapter(
            GamePresenter(cardSize, gameInteractor, coverLoader),
            Game.DIFF_CALLBACK
        )

        this.adapter = pagingAdapter

        launchOnState(Lifecycle.State.RESUMED) {
            gamesViewModel.games
                .collect { pagingAdapter.submitData(lifecycle, it) }
        }

        args.systemIds.let {
            gamesViewModel.systemIds.value = listOf(*it)
        }

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> gameInteractor.onGamePlay(item)
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @dagger.Module
    class Module
}

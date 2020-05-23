package com.eroom.erooja.feature.otherList

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.eroom.data.entity.MinimalTodoListDetail
import com.eroom.domain.globalconst.Consts
import com.eroom.domain.utils.*
import com.eroom.erooja.R
import com.eroom.erooja.databinding.ActivityOthersListBinding
import com.eroom.erooja.dialog.EroojaDialogActivity
import com.eroom.erooja.feature.addDirectList.addMyTodoListPage.AddMyListActivity
import com.eroom.erooja.feature.joinOtherList.joinTodoListPage.JoinOtherListActivity
import org.koin.android.ext.android.get
import timber.log.Timber

class OtherListActivity : AppCompatActivity(),
    OtherListContract.View {
    lateinit var binding: ActivityOthersListBinding
    lateinit var presenter: OtherListPresenter
    private var userTodoList = ArrayList<String>()
    private var userUid = ""
    private var goalId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpDataBinding()
        initView()
    }

    fun setUpDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_others_list)
        binding.othersDetail = this@OtherListActivity
    }

    @SuppressLint("SetTextI18n")
    override fun setAllView(todoList: ArrayList<MinimalTodoListDetail>) {
        binding.othersDetailRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@OtherListActivity)
            adapter = OthersDetailAdapter(todoList)
        }


        repeat(todoList.size) {
            userTodoList.add(todoList[it].content)
        }

        var count = 0
        todoList.forEach { if (it.isEnd) count += 1 }
        binding.participantListText.text =
            "${((count.toDouble() / todoList.size) * 100).toInt()}% 달성중"
    }

    fun initView() {
        if (intent.getBooleanExtra(Consts.IS_FROM_MYPAGE_ONGOING_GOAL, false)
            || intent.getBooleanExtra(Consts.IS_FROM_MYPAGE_ENDED_GOAL, false)
        ) {
            binding.savelistBtn.visibility = View.INVISIBLE
        }
        userUid = intent.getStringExtra(Consts.UID) ?: ""
        goalId = intent.getLongExtra(Consts.GOAL_ID, -1)
        presenter = OtherListPresenter(this, get(), get())
        presenter.getData(userUid, goalId)
        presenter.getProfileImage(userUid)
        binding.usernameList.text = intent.getStringExtra(Consts.NAME)
        binding.goalDateTxt.text = intent.getStringExtra(Consts.DATE)

        statusBarColor(this@OtherListActivity, R.color.subLight3)
    }

   ///////--------------------- + Button 을 눌러 리스트에 참여하기 -----------------------//////
    fun addTodoListBtn() {
       showAlert()

//        val intent = Intent(this@OtherListActivity, JoinOtherListActivity::class.java)
//            .apply {
//                putExtra(Consts.GOAL_ID, intent.getLongExtra(Consts.GOAL_ID, -1))
//                putExtra(Consts.UID, userUid)
//                putExtra(Consts.DATE, binding.goalDateTxt.text)
//                putExtra(Consts.GOAL_TITLE, intent.getStringExtra(Consts.GOAL_TITLE))
//                putExtra(Consts.DESCRIPTION, intent.getStringExtra(Consts.DESCRIPTION))
//                putExtra(Consts.USER_TODO_LIST, userTodoList)
//            }
//        startActivity(intent)
    }

    private fun showAlert(){
        startActivityForResult(
            Intent(
                this,
                EroojaDialogActivity::class.java
            ).apply {
                putExtra(Consts.DIALOG_TITLE, "")
                putExtra(
                    Consts.DIALOG_CONTENT,
                    "이 리스트에 참여하시겠어요?"
                )
                putExtra(Consts.DIALOG_CONFIRM, true)
                putExtra(Consts.DIALOG_CANCEL, true)
            }, 1500
        )
    }

    override fun setProfileImage(imagePath: String?) {
        imagePath?.let {
            binding.circleImageView.loadUrl(it)
            Timber.e(it)
        }
            ?: run {
                binding.circleImageView.loadDrawable(
                    resources.getDrawable(
                        R.drawable.ic_icon_users_blank,
                        null
                    )
                )
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = data?.getBooleanExtra(Consts.DIALOG_RESULT, false) //확인 or 취소

        if(requestCode == 1500 && resultCode == 6000) {
            if (result!!) {
                val intent = Intent(this@OtherListActivity, JoinOtherListActivity::class.java)
                    .apply {
                        putExtra(Consts.GOAL_ID, intent.getLongExtra(Consts.GOAL_ID, -1))
                        putExtra(Consts.UID, userUid)
                        putExtra(Consts.DATE, binding.goalDateTxt.text)
                        putExtra(Consts.GOAL_TITLE, intent.getStringExtra(Consts.GOAL_TITLE))
                        putExtra(Consts.DESCRIPTION, intent.getStringExtra(Consts.DESCRIPTION))
                        putExtra(Consts.USER_TODO_LIST, userTodoList)
                    }
                startActivity(intent)
            } else {
                finish()
            }
        }
    }


    fun back(v: View) {
        finish()
    }

}
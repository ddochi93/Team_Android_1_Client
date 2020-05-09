package com.eroom.erooja.feature.editgoal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.eroom.domain.globalconst.Consts
import com.eroom.domain.utils.vibrateShort
import com.eroom.erooja.R
import com.eroom.erooja.databinding.ActivityEditGoalBinding
import com.eroom.erooja.dialog.EroojaDialogActivity
import rx.Observable

class EditGoalActivity : AppCompatActivity(), EditGoalContract.View, EditGoalAdapter.OnStartDragListener {
    lateinit var presenter: EditGoalPresenter
    lateinit var editGoalBinding: ActivityEditGoalBinding
    lateinit var mEditGoalAdapter: EditGoalAdapter
    lateinit var mDeleteGoalAdapter: DeleteGoalAdapter
    lateinit var mItemEditTouchHelper: ItemTouchHelper
    lateinit var editList: ArrayList<String>
    private var addFragment: Fragment? = null

    private val temporaryDeleteList: ArrayList<Int> = ArrayList()

    private var mMode: EditMode = EditMode.EDIT
    var isDeleteMode: ObservableField<Boolean> = ObservableField(false)
    var isAddMode: ObservableField<Boolean> = ObservableField(false)
    val deletingCount: ObservableField<String> = ObservableField("삭제")
    var isCanDelete: Boolean = false
    var isCanEdit: Boolean = false
    val isButtonActive: ObservableField<Boolean> = ObservableField(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPresenter()
        setUpDataBinding()
        initView()
    }

    private fun initPresenter() {
        presenter = EditGoalPresenter(this)
    }

    private fun setUpDataBinding() {
        editGoalBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_goal)
        editGoalBinding.activity = this
    }

    private fun initView() {
        editList = arrayListOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")
        editRecyclerInit(true)
        deleteRecyclerInit()
        imageListenerInit()
        buttonListenerInit()
    }

    private fun editRecyclerInit(isMovable: Boolean) {
        mEditGoalAdapter = EditGoalAdapter(this, this, this, isMovable)

        editGoalBinding.editGoalRecycler.apply {
            layoutManager = LinearLayoutManager(this@EditGoalActivity)
        }

        val mCallback = EditGoalItemTouchHelperCallback(mEditGoalAdapter)
        mItemEditTouchHelper = ItemTouchHelper(mCallback)

        mItemEditTouchHelper.attachToRecyclerView(null)

        editGoalBinding.editGoalRecycler.adapter = mEditGoalAdapter
    }

    private fun deleteRecyclerInit() {
        mDeleteGoalAdapter = DeleteGoalAdapter(presenter.callback, this, deleteClicked)
        editGoalBinding.deleteGoalRecycler.apply {
            adapter = mDeleteGoalAdapter
            layoutManager = LinearLayoutManager(this@EditGoalActivity)
        }
    }

    fun attachRecyclerView() {
        mItemEditTouchHelper.attachToRecyclerView(editGoalBinding.editGoalRecycler)
    }

    fun detachRecyclerView() {
        mItemEditTouchHelper.attachToRecyclerView(null)
    }

    override fun onStartDrag(holder: EditGoalViewHolder) {
        mItemEditTouchHelper.startDrag(holder)
    }

    private fun imageListenerInit() {
        editGoalBinding.deleteImage.setOnClickListener {
            when (mMode) {
                EditMode.DELETE -> {
                    mMode = EditMode.EDIT
                    isDeleteMode.set(false)
                    isButtonActive.set(isCanEdit)
                    temporaryDeleteList.clear()
                    deletingCount.set("삭제")
                    isCanDelete = false
                    (it as ImageView).setImageDrawable(getDrawable(R.drawable.ic_icon_navi_trash))
                    editGoalBinding.plusImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_plus_normal))
                    editGoalBinding.editGoalRecycler.visibility = View.VISIBLE
                    editGoalBinding.deleteGoalRecycler.visibility = View.GONE
                }
                EditMode.ADD -> {}
                EditMode.EDIT -> {
                    mMode = EditMode.DELETE
                    isDeleteMode.set(true)
                    isButtonActive.set(isCanDelete)
                    (it as ImageView).setImageDrawable(getDrawable(R.drawable.ic_icon_check_active))
                    editGoalBinding.plusImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_plus_inactive))
                    editGoalBinding.editGoalRecycler.visibility = View.GONE
                    editGoalBinding.deleteGoalRecycler.visibility = View.VISIBLE
                }
            }
            mDeleteGoalAdapter.notifyDataSetChanged()
            mEditGoalAdapter.notifyDataSetChanged()
        }
        editGoalBinding.plusImage.setOnClickListener {
            when (mMode) {
                EditMode.DELETE -> {}
                EditMode.ADD -> {
                    mMode = EditMode.EDIT
                    isAddMode.set(false)
                    editRecyclerInit(true)
                    (it as ImageView).setImageDrawable(getDrawable(R.drawable.ic_icon_navi_plus_normal))
                    editGoalBinding.deleteImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_trash))
                    addFragment?.let { fragment ->
                        (addFragment as AddListFragment).goalList.value?.let { addList ->
                            editList.addAll(addList)
                        }
                        (addFragment as AddListFragment).writingText.value?.let {string ->
                            if (string.isNotEmpty()) editList.add(string)
                        }
                        supportFragmentManager.beginTransaction().detach(fragment).commit()
                    }
                    addFragment = null
                }
                EditMode.EDIT -> {
                    mMode = EditMode.ADD
                    isAddMode.set(true)
                    editRecyclerInit(false)
                    (it as ImageView).setImageDrawable(getDrawable(R.drawable.ic_icon_check_active))
                    editGoalBinding.deleteImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_trash_inactive))
                    addFragment = AddListFragment.newInstance()
                    addFragment?.let { fragment ->
                        supportFragmentManager.beginTransaction()
                            .add(R.id.addTodoFrame, fragment).commit()
                    }
                }
            }
            mEditGoalAdapter.notifyDataSetChanged()
            mDeleteGoalAdapter.notifyDataSetChanged()
        }
    }

    private fun buttonListenerInit() {
        editGoalBinding.editCompleteButton.setOnClickListener {
            when (mMode) {
                EditMode.DELETE -> {
                    if (temporaryDeleteList.size == editList.size) {
                        Thread(Runnable {
                            this.vibrateShort()
                        }).start()
                        startActivityForResult(Intent(
                            this,
                            EroojaDialogActivity::class.java
                        ).apply {
                            putExtra(Consts.DIALOG_TITLE, "")
                            putExtra(
                                Consts.DIALOG_CONTENT,
                                "적어도 리스트 내 1개 이상의 항목이 필요합니다."
                            )
                            putExtra(Consts.DIALOG_CONFIRM, true)
                            putExtra(Consts.DIALOG_CANCEL, false)
                        }, 4000)
                    } else {
                        startActivityForResult(
                            Intent(
                                this,
                                EroojaDialogActivity::class.java
                            ).apply {
                                putExtra(Consts.DIALOG_TITLE, "")
                                putExtra(
                                    Consts.DIALOG_CONTENT,
                                    "리스트를 삭제했을 경우, 달성률이 변동될 수 있습니다. 정말 삭제하시겠어요?"
                                )
                                putExtra(Consts.DIALOG_CONFIRM, true)
                                putExtra(Consts.DIALOG_CANCEL, true)
                            }, 3000
                        )
                    }
                }
                EditMode.ADD -> {}
                EditMode.EDIT -> {

                }
            }
        }
    }

    private val deleteClicked = { index: Int , isChecked: Boolean ->
        isCanDelete = if (isChecked) {
            temporaryDeleteList.add(index)
            deletingCount.set("${temporaryDeleteList.size}개 삭제")
            true
        } else {
            temporaryDeleteList.remove(index)
            if (temporaryDeleteList.size == 0) {
                deletingCount.set("삭제")
                false
            } else {
                deletingCount.set("${temporaryDeleteList.size}개 삭제")
                true
            }
        }
        isButtonActive.set(isCanDelete)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3000 && resultCode == 6000) {
            data?.let {
                val result = it.getBooleanExtra(Consts.DIALOG_RESULT, false)
                if (result) {
                    temporaryDeleteList.sort()
                    temporaryDeleteList.reverse()
                    temporaryDeleteList.forEach { index: Int ->
                        editList.removeAt(index)
                        mDeleteGoalAdapter.notifyItemRemoved(index)
                        mDeleteGoalAdapter.notifyItemRangeChanged(index, editList.size - 1)
                        mEditGoalAdapter.notifyItemRemoved(index)
                        mEditGoalAdapter.notifyItemRangeChanged(index, editList.size - 1)
                    }
                    temporaryDeleteList.clear()
                    deletingCount.set("삭제")
                    isCanDelete = false
                    isButtonActive.set(isCanDelete)
                }
            }
        }
        if (requestCode == 5000 && resultCode == 6000) {
            data?.let {
                val result = it.getBooleanExtra(Consts.DIALOG_RESULT, false)
                if (result) {
                    mMode = EditMode.EDIT
                    isAddMode.set(false)
                    editRecyclerInit(true)
                    editGoalBinding.plusImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_plus_normal))
                    editGoalBinding.deleteImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_trash))
                    addFragment?.let { fragment ->
                        (addFragment as AddListFragment).goalList.value?.let { addList ->
                            editList.addAll(addList)
                        }
                        (addFragment as AddListFragment).writingText.value?.let { string ->
                            if (string.isNotEmpty()) editList.add(string)
                        }
                        supportFragmentManager.beginTransaction().detach(fragment).commit()
                    }
                    addFragment = null
                } else {
                    mMode = EditMode.EDIT
                    isAddMode.set(false)
                    editRecyclerInit(true)
                    editGoalBinding.plusImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_plus_normal))
                    editGoalBinding.deleteImage.setImageDrawable(getDrawable(R.drawable.ic_icon_navi_trash))
                    addFragment?.let { fragment ->
                        supportFragmentManager.beginTransaction().detach(fragment).commit()
                    }
                    addFragment = null
                }
            }
        }
    }

    fun showKeyboard(input: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(input, 0)
    }

    override fun onBackPressed() {
        when (mMode) {
            EditMode.DELETE -> {
                editGoalBinding.deleteImage.performClick()
            }
            EditMode.ADD -> {
                startActivityForResult(Intent(
                    this,
                    EroojaDialogActivity::class.java
                ).apply {
                    putExtra(Consts.DIALOG_TITLE, "")
                    putExtra(
                        Consts.DIALOG_CONTENT,
                        "나가기 전 추가한 내역을 저장하시겠어요?"
                    )
                    putExtra(Consts.DIALOG_CONFIRM, true)
                    putExtra(Consts.DIALOG_CANCEL, true)
                }, 5000)
            }
            EditMode.EDIT -> {
                finish()
            }
        }
    }
}

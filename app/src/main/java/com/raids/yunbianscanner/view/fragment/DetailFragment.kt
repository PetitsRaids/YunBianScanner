package com.raids.yunbianscanner.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

import com.raids.yunbianscanner.R
import com.raids.yunbianscanner.data.model.History
import com.raids.yunbianscanner.data.viewmodel.HistoryViewModel
import com.raids.yunbianscanner.support.utils.MyUtils

class DetailFragment : Fragment() {

    private lateinit var goodsName: TextView
    private lateinit var goodsPrice: TextView
    private lateinit var goodsDescribe: TextView
    private lateinit var barcode: TextView
    private lateinit var copyBarcode: Button
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        val codeNumber = arguments?.getString(MyUtils.BARCODE_BUNDLE)
        barcode.text = codeNumber.toString()
        historyViewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(activity!!.application).create(HistoryViewModel::class.java)
        val history = History()
        history.goodsName = "图解HTTP"
        history.goodsPrice = 49.00
        history.describe = "《图解HTTP》 是一部由[日]上野宣 所著书籍，适合Web开发工程师，以及对HTTP协议感兴趣的各层次读者。"
        history.barcode = "9787115351531"
        historyViewModel.insertHistory(history)
        goodsName.text = history.goodsName
        goodsPrice.text = history.goodsPrice.toString()
        goodsDescribe.text = history.describe
        barcode.text = history.barcode
    }

    private fun init() {
        goodsName = view!!.findViewById(R.id.goods_name)
        goodsPrice = view!!.findViewById(R.id.goods_price)
        goodsDescribe = view!!.findViewById(R.id.goods_describe)
        barcode = view!!.findViewById(R.id.barcode)
        copyBarcode = view!!.findViewById(R.id.copy_barcode)
    }
}

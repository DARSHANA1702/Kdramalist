package com.bapercoding.simplecrud


import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_photo.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentPhoto : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(
            view: View,
            @Nullable savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val listPhoto = ArrayList<Int>()
        listPhoto.add(R.drawable.comingsoon)
        rvPhoto.setHasFixedSize(true)
        rvPhoto.layoutManager = LinearLayoutManager(context)
        val adapter = context?.let { PhotoFilmAdapter(it,listPhoto) }
        adapter?.notifyDataSetChanged()
        rvPhoto.adapter = adapter
    }
}

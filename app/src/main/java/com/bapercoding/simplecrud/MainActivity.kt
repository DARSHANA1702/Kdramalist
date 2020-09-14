@file:Suppress("DEPRECATION")

package com.bapercoding.simplecrud

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SmoothScroller
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    var arrayList = ArrayList<Kdramas>()
    var arrayList2 = ArrayList<String>()
    private var list: java.util.ArrayList<Film> = arrayListOf()
    val search = Search()
    private lateinit var etSearch: EditText
    var isShow = false
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dbReference2: DatabaseReference
    private lateinit var firebaseDatabase2: FirebaseDatabase
    var thisActivity: Activity = this
    var iterator: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.custom_action_bar)

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = ""

        list.addAll(Data.listData)
        searchLayout.visibility = View.GONE
        isShow = false
        tv_nothing.visibility = View.GONE
        btn_back_to_top.visibility = View.GONE

        mRecyclerView1.setHasFixedSize(true)
        mRecyclerView1.layoutManager = LinearLayoutManager(this)
        mRecyclerView1.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

                //show button when not on top
                val visibility = if ((mRecyclerView1.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() !== 0){
                    View.VISIBLE
                }
                else {
                    View.GONE
                }
                btn_back_to_top.visibility = visibility

                if(btn_back_to_top.visibility == View.VISIBLE){
                    val buttonTimer = Timer()
                    buttonTimer.schedule(object : TimerTask() {
                        override fun run() {
                            runOnUiThread { btn_back_to_top.visibility = View.GONE }
                        }
                    }, 6000)
                }

                //hide layout when scroll down
                if (dy > 0){
                    val dialog: LinearLayout = findViewById(R.id.searchLayout)
                    if(dialog.visibility == View.VISIBLE){
                        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_hide)
                        animation.duration = 500
                        dialog.animation = animation
                        dialog.animate()
                        animation.start()
                        dialog.visibility = LinearLayout.GONE
                    }
                    btn_back_to_top.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward_white_24dp,0,0,0)
                    //smooth scroll
                    val smoothScroller: SmoothScroller = object : LinearSmoothScroller(applicationContext) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_END
                        }
                    }

                    btn_back_to_top.setOnClickListener{
                        smoothScroller.targetPosition = arrayList.size
                        mRecyclerView1.layoutManager.startSmoothScroll(smoothScroller)
                        btn_back_to_top.visibility = View.GONE
                    }
                }
                else if(dy < 0){
                    btn_back_to_top.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward_white_24dp,0,0,0)
                    //smooth scroll
                    val smoothScroller: SmoothScroller = object : LinearSmoothScroller(applicationContext) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }

                    btn_back_to_top.setOnClickListener{
                        smoothScroller.targetPosition = 0
                        mRecyclerView1.layoutManager.startSmoothScroll(smoothScroller)
                        btn_back_to_top.visibility = View.GONE
                    }
                }
            }
        })

        etSearch = findViewById(R.id.mytextText)
        btn_search.setOnClickListener {
            val loading = ProgressDialog(this)
            loading.setMessage("Mencari data...")
            loading.show()
            search.listSearch.clear()
            search.list2.clear()
            search.list3.clear()
            search.searchJudul(etSearch.text.toString(), arrayList,list, arrayList2)
            loading.dismiss()
            if(search.listSearch.size == 0){
                tv_nothing.visibility = View.VISIBLE
                tv_nothing.text = getString(R.string.nothing_found)
            }
            else{
                tv_nothing.visibility = View.GONE
            }
            val adapter2 = RVAAdapterStudent(thisActivity, applicationContext, search.listSearch, search.list2, search.list3)
            adapter2.notifyDataSetChanged()
            mRecyclerView1.adapter = adapter2

            val dialog: LinearLayout = findViewById(R.id.searchLayout)
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_hide)
            animation.duration = 500
            dialog.animation = animation
            dialog.animate()
            animation.start()
            dialog.visibility = LinearLayout.GONE

            //close virtual keyboard
            closeKeyBoard()
        }

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        dbReference = firebaseDatabase.getReference("users2")
        if (user != null) {
            val name = intent.getStringExtra("username")
            val id = user.uid
            val email2 = user.email
            val user2 = UserInfo(name, email2)
            if(!TextUtils.isEmpty(name) && !(name.contains("@"))){
                dbReference.child(id).setValue(user2)
            }
        }

        getImagepage()
    }
    fun getImagepage(){
        val dbReference2 = FirebaseDatabase.getInstance().getReference("imagesPage")
        val postListener2 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data: DataSnapshot in dataSnapshot.children){
                    val hasil = data.getValue(Upload::class.java)
                    arrayList2.add(hasil?.url!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        dbReference2.addValueEventListener(postListener2)
    }

    override fun onResume() {
        super.onResume()
        loadAllStudents()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.about_item) {
            val intentAboutMe = Intent(this@MainActivity, AboutMe::class.java)
            startActivity(intentAboutMe)
            overridePendingTransition(R.anim.enter, R.anim.exit)
            finish()
            searchLayout.visibility = View.GONE
        }
        if(id == R.id.search_item){
            etSearch = findViewById(R.id.mytextText)
            etSearch.text = null
            etSearch.hint = getString(R.string.find_korean_dramas)

            val dialog: LinearLayout = findViewById(R.id.searchLayout)
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_show)
            dialog.bringToFront()
            dialog.requestLayout()
            animation.duration = 500
            dialog.animation = animation
            dialog.animate()
            animation.start()
            dialog.visibility = LinearLayout.VISIBLE
        }
        return super.onOptionsItemSelected(item)
    }


    private fun loadAllStudents(){

        val loading = ProgressDialog(this)
        loading.setMessage("Memuat data...")
        loading.show()
        iterator += 1
        val db = FirebaseFirestore.getInstance()
        db.collection("kdramas")
                .get()
                .addOnSuccessListener { result ->
                    arrayList.clear()
                    for (document in result) {
                        arrayList.add(Kdramas(document.getString("judul")!!,
                                document.getString("rating"),
                                document.getString("episode")!!,
                                document.getString("sinopsis")))
                    }
                    getImagepage()
                    loading.dismiss()
                    if(arrayList2.isNotEmpty() && iterator <= 2){
                        loading.dismiss()
                        val adapter = RVAAdapterStudent(thisActivity, applicationContext, arrayList, list, arrayList2)
                        adapter.notifyDataSetChanged()
                        mRecyclerView1.adapter = adapter
                    }
                    else if(iterator <= 2){
                        loading.dismiss()
                        loadAllStudents()
                    }
                    else if(iterator > 2){
                        loading.dismiss()
                        iterator = 0
                        val snackBar = Snackbar.make(
                                currentFocus!!, "    Connection Failure",
                                Snackbar.LENGTH_INDEFINITE
                        )
                        val snackBarView = snackBar.view
                        snackBarView.setBackgroundColor(Color.BLACK)
                        val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                        textView.setTextColor(Color.WHITE)
                        textView.setTextSize(16F)
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.warning, 0, 0, 0)
                        val snack_action_view = snackBarView.findViewById<Button>(android.support.design.R.id.snackbar_action)
                        snack_action_view.setTextColor(Color.YELLOW)

                        // Set an action for snack bar
                        snackBar.setAction("Retry") {
                            loadAllStudents()

                        }
                        snackBar.show()
                    }
                }
                .addOnFailureListener { exception ->
                    loading.dismiss()
                    Log.d("Error", "Error getting documents: ", exception)
                    val snackBar = Snackbar.make(
                            currentFocus!!, "    Connection Failure",
                            Snackbar.LENGTH_INDEFINITE
                    )
                    val snackBarView = snackBar.view
                    snackBarView.setBackgroundColor(Color.BLACK)
                    val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                    textView.setTextColor(Color.WHITE)
                    textView.setTextSize(16F)
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.warning, 0, 0, 0)
                    val snack_action_view = snackBarView.findViewById<Button>(android.support.design.R.id.snackbar_action)
                    snack_action_view.setTextColor(Color.YELLOW)

                    // Set an action for snack bar
                    snackBar.setAction("Retry") {
                        loadAllStudents()

                    }
                    snackBar.show()
                }
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

//write to cloud firestore
//val kdrama = hashMapOf(
//        "judul" to jsonObject.getString("judul"),
//        "rating" to jsonObject.getString("rating"),
//        "episode" to jsonObject.getString("episode"),
//        "sinopsis" to jsonObject.getString("sinopsis")
//)
//
//db.collection("kdramas").document(jsonObject.getString("judul"))
//.set(kdrama)
//.addOnSuccessListener { Log.d("Message", "DocumentSnapshot successfully added!") }
//.addOnFailureListener { e -> Log.w("Message ", "Error adding document", e) }

//read from web api
//AndroidNetworking.get(ApiEndPoint.READ2)
//.setPriority(Priority.MEDIUM)
//.build()
//.getAsJSONObject(object : JSONObjectRequestListener{
//
//    override fun onResponse(response: JSONObject?) {
//
//        arrayList.clear()
//
//        val jsonArray = response?.optJSONArray("result")
//
//        if(jsonArray?.length() == 0){
//            loading.dismiss()
//            Toast.makeText(applicationContext,"Student data is empty, Add the data first",Toast.LENGTH_SHORT).show()
//        }
//
//        for(i in 0 until jsonArray?.length()!!){
//
//            val jsonObject = jsonArray.optJSONObject(i)
//            arrayList.add(Kdramas(jsonObject.getString("judul"),
//                    jsonObject.getString("rating"),
//                    jsonObject.getString("episode"),
//                    jsonObject.getString("sinopsis")))
//
//            if(jsonArray.length() - 1 == i){
//
//                loading.dismiss()
//                val adapter = RVAAdapterStudent(applicationContext,arrayList,list)
//                adapter.notifyDataSetChanged()
//                mRecyclerView1.adapter = adapter
//
//            }
//
//        }
//
//    }
//
//    override fun onError(anError: ANError?) {
//        loading.dismiss()
//        Log.d("ONERROR",anError?.errorDetail?.toString())
//        val snackBar = Snackbar.make(
//                currentFocus, "    Connection Failure",
//                Snackbar.LENGTH_INDEFINITE
//        )
//        val snackBarView = snackBar.view
//        snackBarView.setBackgroundColor(Color.BLACK)
//        val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
//        textView.setTextColor(Color.WHITE)
//        textView.setTextSize(16F)
//        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.warning, 0, 0, 0)
//        val snack_action_view = snackBarView.findViewById<Button>(android.support.design.R.id.snackbar_action)
//        snack_action_view.setTextColor(Color.YELLOW)
//
//        // Set an action for snack bar
//        snackBar.setAction("Retry") {
//            loadAllStudents()
//
//        }
//        snackBar.show()
//    }
//})

//write image url to realtime database
//firebaseDatabase2 = FirebaseDatabase.getInstance()
//dbReference2 = firebaseDatabase2.getReference("images")
//val storageReference = FirebaseStorage.getInstance().reference
//val iterator = arrayOf('i','k','c','g','a','b','e','h','j','d','f')
//val iterator2 = arrayOf('a','b','c','d','e','f','g','h','i','j')
//for(position in 0 until (iterator.size)){
//    for(position2 in iterator2.indices){
//        val nama = "${iterator[position]}${iterator2[position2]}"
//        val ref = storageReference.child("images/${arrayList[position].judul}/" + nama + ".jpg")
//        ref.downloadUrl.addOnSuccessListener {
//            writeNewImageInfoToDB(position, nama, it.toString())
//        }.addOnFailureListener {  }
//        if(nama == "if"){
//            val nama2 = "${iterator[position]}${iterator2[position2]}${iterator2[position2]}"
//            val ref2 = storageReference.child("images/${arrayList[position].judul}/" + nama2 + ".jpg")
//            ref2.downloadUrl.addOnSuccessListener {
//                writeNewImageInfoToDB(position, nama2, it.toString())
//            }.addOnFailureListener {  }
//        }
//    }
//}
//private fun writeNewImageInfoToDB(letak: Int, name: String, url: String) {
//    val info = Upload(name, url)
//    val key: String? = dbReference2.push().getKey()
//    var judul = arrayList[letak].judul
//    if(judul.contains(".")){
//        judul = judul.replace(".", "")
//        dbReference2.child(judul).child(key!!).setValue(info)
//    }
//    else{
//        dbReference2.child(judul).child(key!!).setValue(info)
//    }
//}

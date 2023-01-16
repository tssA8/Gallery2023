package com.wallpaper.gallery.gallery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.Adapter.ChoosePictureAdapter
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Provider

class ChooseWallpaperActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var recycleViewChooseWallpaper: RecyclerView
    lateinit var chooseWallpaperAdapter: ChoosePictureAdapter
    lateinit var pictureList: ArrayList<Int>

    @Inject lateinit var imageLoaderProvider: Provider<ImageLoader>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_wallpaper)

        initPictureList()
        val gridLayoutManager = GridLayoutManager(this, 3)
        recycleViewChooseWallpaper = findViewById(R.id.rl_choose_wallpaper)
        recycleViewChooseWallpaper.setLayoutManager(gridLayoutManager)
        chooseWallpaperAdapter = ChoosePictureAdapter(this, imageLoaderProvider.get(), pictureList) { choosePics ->
            val pictureId = choosePics.pictureId
            Log.d("ChooseWallpaperActivity", " pictureId : $pictureId")
            startForResult(pictureId)
        }
        recycleViewChooseWallpaper.adapter = chooseWallpaperAdapter
        chooseWallpaperAdapter.notifyDataSetChanged()
    }

    private fun initPictureList() {
        pictureList.add(R.drawable.cyberpunk2)
        pictureList.add(R.drawable.pic1)
        pictureList.add(R.drawable.pic2)
        pictureList.add(R.drawable.pic6)
        pictureList.add(R.drawable.pic4)
        pictureList.add(R.drawable.pic5)
        pictureList.add(R.drawable.pic6)
        pictureList.add(R.drawable.pic7)
        pictureList.add(R.drawable.wolf)
        pictureList.add(R.drawable.bimax)
    }

    private fun startForResult(picPath: Int) {
        val intent = Intent().apply {
            this.putExtra(Constant.CHOOSE_PICTURE_EXTRA, picPath)
        }
        setResult(RESULT_OK, intent)
    }
}
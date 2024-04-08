package com.umcbms.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException

class DemoExActivity : AppCompatActivity() {

    private lateinit var typeface: Typeface

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_ex)

        val a = findViewById<TextView>(R.id.a)
        val b = findViewById<TextView>(R.id.b)
        val c = findViewById<TextView>(R.id.c)
        val d = findViewById<TextView>(R.id.d)
        val e = findViewById<TextView>(R.id.e)
        val imageViewSvg = findViewById<ImageView>(R.id.imageViewSvg)

        // typeface = Typeface.createFromAsset(applicationContext.assets, "regular.ttf")
        typeface = ResourcesCompat.getFont(this, R.font.abc900)!!

        a.typeface = typeface
        b.typeface = typeface
        c.typeface = typeface
        d.typeface = typeface
        e.typeface = typeface

        val svgString =
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\"><g id=\"SVGRepo_bgCarrier\" stroke-width=\"0\"></g><g id=\"SVGRepo_tracerCarrier\" stroke-linecap=\"round\" stroke-linejoin=\"round\"></g><g id=\"SVGRepo_iconCarrier\"> <path d=\"M21 15.5018C18.651 14.5223 17 12.2039 17 9.5C17 6.79774 18.6534 4.48062 21 3.5C20.2304 3.17906 19.3859 3 18.5 3C15.7977 3 13.4806 4.64899 12.5 6.9956M6.9 21C4.74609 21 3 19.2889 3 17.1781C3 15.4286 4.3 13.8125 6.25 13.5C6.86168 12.0617 8.30934 11 9.9978 11C12.1607 11 13.9285 12.6589 14.05 14.75C15.1978 15.2463 16 16.4645 16 17.7835C16 19.5599 14.5449 21 12.75 21L6.9 21Z\" stroke=\"#000000\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"></path> </g></svg>"
        val drawable = getDrawableFromSvgString(svgString, R.color.blue, this)
        imageViewSvg.setImageDrawable(drawable)

    }

    private fun getDrawableFromSvgString(
        svgString: String,
        colorResId: Int,
        context: Context
    ): Drawable? {
        return try {

            val color = ContextCompat.getColor(context, colorResId)

            val svgWithColor = svgString.replace(
                "stroke=\"#000000\"",
                "stroke=\"${String.format("#%06X", 0xFFFFFF and color)}\""
            )

            val svg = SVG.getFromString(svgWithColor)
            val drawable = PictureDrawable(svg.renderToPicture())

            drawable
        } catch (e: SVGParseException) {
            e.printStackTrace()
            null
        }
    }


    /*
        private fun getDrawableFromSvgString(svgString: String): Drawable? {
            return try {
                val svg = SVG.getFromString(svgString)
                val drawable = PictureDrawable(svg.renderToPicture())
                drawable
            } catch (e: SVGParseException) {
                e.printStackTrace()
                null
            }
        }*/

}
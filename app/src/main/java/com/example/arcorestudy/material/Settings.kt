package com.example.arcorestudy.material

import android.content.Context
import android.content.SharedPreferences
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import java.util.concurrent.atomic.AtomicBoolean

class Settings(context: Context) {

    companion object : Singleton<Settings, Context>(::Settings)

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    open class AtomicBooleanPref(defaultValue : Boolean, private val key: String, private val prefs: SharedPreferences){
        private val value: AtomicBoolean = AtomicBoolean(prefs.getBoolean(key, defaultValue))

        fun get() = value.get()

        fun toggle(){
            val newValue = get().not()
            value.set(newValue)
            prefs.edit().putBoolean(key, newValue).apply()
        }
    }

    class Sunlight(defaultValue: Boolean, key: String, prefs: SharedPreferences) : AtomicBooleanPref(defaultValue, key, prefs){

        fun toggle(menuItem: MenuItem, arSceneView: ArSceneView){
            toggle()
            applyTo(menuItem)
            applyTo(arSceneView)
        }

        fun applyTo(arSceneView: ArSceneView){
            arSceneView.scene?.sunlight?.isEnabled = get()
        }

        fun applyTo(menuItem: MenuItem){
            menuItem.isChecked = get()
        }
    }

    class Shadows(defaultValue: Boolean, key: String, prefs: SharedPreferences) : AtomicBooleanPref(defaultValue, key, prefs) {

        fun toggle(menuItem: MenuItem, arSceneView: ArSceneView) {
            toggle()
            applyTo(menuItem)
            applyTo(arSceneView)
        }

        fun applyTo(arSceneView: ArSceneView) {
            val value = get()
            arSceneView.scene?.callOnHierarchy {
                it.renderable?.apply {
                    isShadowCaster = value
                    isShadowReceiver = value
                }
            }
        }

        fun applyTo(menuItem: MenuItem) {
            menuItem.isChecked = get()
        }

    }

    class Planes(defaultValue: Boolean, key: String, prefs: SharedPreferences) : AtomicBooleanPref(defaultValue, key, prefs) {

        fun toggle(menuItem: MenuItem, arSceneView: ArSceneView) {
            toggle()
            applyTo(menuItem)
            applyTo(arSceneView)
        }

        fun applyTo(arSceneView: ArSceneView) {
            arSceneView.planeRenderer?.isEnabled = get()
        }

        fun applyTo(menuItem: MenuItem) {
            menuItem.isChecked = get()
        }

    }

    class Selection(defaultValue: Boolean, key: String, prefs: SharedPreferences) : AtomicBooleanPref(defaultValue, key, prefs) {

        fun toggle(menuItem: MenuItem, selectionVisualizer: Footprint) {
            toggle()
            applyTo(menuItem)
            applyTo(selectionVisualizer)
        }

        fun applyTo(selectionVisualizer: Footprint) {
            selectionVisualizer.isEnabled = get()
        }

        fun applyTo(menuItem: MenuItem) {
            menuItem.isChecked = get()
        }

    }

    class Reticle(defaultValue : Boolean, key : String, prefs: SharedPreferences) : AtomicBooleanPref(defaultValue, key, prefs){

        class Node(context: Context) : com.google.ar.sceneform.Node(){

            companion object {
                val INVISIBLE_SCALE: Vector3 = Vector3.zero()
                val VISIBLE_DEFAULT_SCALE: Vector3 = Vector3.one()
                val VISIBLE_PLANE_SCALE: Vector3 = Vector3.one()
                val VISIBLE_DEPTH_POINT_SCALE: Vector3 = Vector3.one().scaled(0.5F)
            }

            private var properties: MaterialProperties = MaterialProperties()

            init {
                // todo 아래 아직 있음
            }
        }

    }

}
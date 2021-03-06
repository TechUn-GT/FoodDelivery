package com.aesc.fooddelivery.ui.activities

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.aesc.fooddelivery.R
import com.aesc.fooddelivery.extensions.amountConverter
import com.aesc.fooddelivery.extensions.loadByURL
import com.aesc.fooddelivery.providers.database.models.Favorites
import com.aesc.fooddelivery.providers.database.viewmodel.MainViewModelFavorites
import com.aesc.fooddelivery.providers.services.models.Producto
import com.aesc.fooddelivery.providers.services.viewmodel.MainViewModel
import com.aesc.fooddelivery.ui.adapters.RecomendadosAdapter
import com.aesc.fooddelivery.utils.Utils
import kotlinx.android.synthetic.main.activity_detail_product.*
import kotlinx.android.synthetic.main.item_categorias_details.view.*

class DetailProductActivity : AppCompatActivity(), View.OnClickListener {
    private var item: Producto? = null
    private var itemID = 0
    lateinit var viewModels: MainViewModel
    lateinit var viewModal: MainViewModelFavorites
    private lateinit var adapter: RecomendadosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_product)
        viewModels = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get()
        iniViews()
    }

    private fun iniViews() {
        // setup animation
        item = intent.extras!!.getParcelable("values")
        itemID = item!!.id.toInt()
        viewModal.search(itemID)
        viewModal.search.observe(this, { list ->
            if (list.isNotEmpty()) {
                animation_view.speed = 1f
                animation_view.playAnimation()
            }
        })
        animation_view.setOnClickListener(this)
        detail_movie_img.loadByURL(item!!.url_imagen)
        tvNombre.text = item!!.nombre
        tvAmount.amountConverter(item!!.precio)
        tvDescripcion.text = item!!.descripcion
        detail_movie_img.animation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)
        logic()
    }

    private fun logic() {
        var status = false
        viewModels.responseProducts.observe(this, {
            if (!status) {
                Utils.logsUtils("SUCCESS $it")
                recyclerviewInit(it.productos)
            }
        })

        viewModels.errorMessage.observe(this, {
            if (!status) {
                Utils.logsUtils("ERROR $it")
            }
        })

        viewModels.loading.observe(this, {
            status = it
            /* if (it) {
                 fragmentProgressBar.visibility = View.VISIBLE
             } else {
                 fragmentProgressBar.visibility = View.GONE
             }*/
        })
        viewModels.products()
    }

    private fun recyclerviewInit(datos: List<Producto>) {
        adapter = RecomendadosAdapter(this, this)
        adapter.setCategories(datos)
        recyclerviewFoods.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerviewFoods.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.animation_view -> {
                var status = false
                viewModal.search(itemID)
                viewModal.search.observe(this, { list ->
                    if (list.isEmpty()) {
                        if (!status) {
                            v.animation_view.speed = 1f
                            v.animation_view.playAnimation()
                            viewModal.addFavorite(
                                Favorites(
                                    id_product = item!!.id.toInt(),
                                    nombre_product = item!!.nombre,
                                    descripcion_product = item!!.descripcion,
                                    precio_product = item!!.precio,
                                    url_imagen_product = item!!.url_imagen
                                )
                            )
                            Utils.logsUtils("Agregado")
                            Toast.makeText(this, "Agregado a Favoritos", Toast.LENGTH_SHORT).show()
                            status = true
                        }
                    } else {
                        if (!status) {
                            v.animation_view.speed = 0f
                            v.animation_view.playAnimation()
                            viewModal.deleteFavorite(list[0])
                            list.forEach {
                                Utils.logsUtils("Removido")
                                Toast.makeText(this, "Se Removio de Favoritos", Toast.LENGTH_SHORT).show()
                            }
                            status = true
                        }
                    }
                })
            }
        }
    }
}
package com.example.weather_application

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.icu.number.NumberFormatter.with
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.weather_application.databinding.FragmentNoExtraWeatherBinding
import com.google.android.material.color.utilities.Score.score
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NoExtraWeatherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoExtraWeatherFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentNoExtraWeatherBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNoExtraWeatherBinding.inflate(inflater, container, false)

        /*val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        viewModel.getWeather()

        viewModel.myResponse.observe(viewLifecycleOwner, Observer {
            // Do something
        })*/

        binding.cardTopTitle.isVisible = false
        binding.nestedScroll.isVisible = false

        return binding.root
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle: Bundle? = arguments

        val zoomInButton = view.findViewById<Button>(R.id.switchToOutZoomedView)
        zoomInButton.setOnClickListener {
            val bundle = Bundle()
            if(view.findViewById<CardView>(R.id.cardTopTitle).isVisible) {
                bundle.putString("cityName", view.findViewById<TextView>(R.id.cityName).text.toString())
            } else {
                bundle.putString("cityName", "")
            }

            val fragment = NoExtraWeatherFragment()
            fragment.arguments = bundle

            Navigation.findNavController(view).navigate(R.id.action_noExtraWeatherFragment_to_extraWeatherForOldPeople, bundle)
        }

        val okHttpClient = OkHttpClient()

        var jsonObject: JSONObject? = null

        fun findWeather() {
            if(binding.inputNameOfCityField.text.toString() != "") {
                val request = Request.Builder()
                    .url("https://api.openweathermap.org/data/2.5/weather?q=${binding.inputNameOfCityField.text.toString().replace(" ", "")}&APPID=86bf0cf4d58ee4766918dd55077e95ab&units=metric&lang=pl")
                    .build()

                var isResponse = false

                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(view.context, "Nieprawidłowe dane!\nNie prawidłowa lokalizacja!", Toast.LENGTH_SHORT).show()
                            binding.inputNameOfCityField.setText("")
                        })
                    }

                    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                Handler(Looper.getMainLooper()).post(Runnable {
                                    Toast.makeText(
                                        view.context,
                                        "Wprowadź poprawną lokalizację!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.inputNameOfCityField.setText("")
                                })
                                throw IOException("Unexpected code $response")
                            }

                            jsonObject = response.body?.string()?.let { it1 -> JSONObject(it1) }

                            val main = jsonObject?.getJSONObject("main")
                            val sys = jsonObject?.getJSONObject("sys")
                            val weather = jsonObject?.getJSONArray("weather")
                            val wind = jsonObject?.getJSONObject("wind")

                            Handler(Looper.getMainLooper()).post(Runnable {
                                binding.inputNameOfCityField.text?.clear()

                                val simpleDate = SimpleDateFormat("HH:mm:ss")
                                val longDate = SimpleDateFormat("dd.M.yyyy \nHH:mm:ss")

                                val timezone = jsonObject?.getLong("timezone")

                                val timeUTC = Date().time
                                val offSet = TimeZone.getDefault().getOffset(timeUTC)

                                binding.cityName.text = jsonObject?.getString("name").toString()
                                binding.dateAndTime.text = longDate.format(timeUTC - offSet + timezone!! * 1000).toString()

                                val temp = main?.getInt("temp")

                                //binding.temperatureField.text = temp.toString() + "\u00B0C"
                                binding.temperatureField2.text = temp.toString() + "\u00B0C"

                                binding.temperatureFieldColor.setBackgroundResource(findColorFromTemp(temp))

                                /*Picasso.get().load("https://openweathermap.org/img/wn/${weather?.getJSONObject(0)?.getString("icon")}@2x.png")
                                    .fit()
                                    .into(binding.iconOfWeather)*/

                                Picasso.get().load("https://openweathermap.org/img/wn/${weather?.getJSONObject(0)?.getString("icon")}@2x.png")
                                    .fit()
                                    .into(binding.iconOfWeather2)

                                val tempMin = main?.getInt("temp_min")
                                val tempMax = main?.getInt("temp_max")

                                binding.minTempField.text = "Min:\n" + tempMin.toString() + "\u00B0C"
                                binding.maxTempField.text = "Max:\n" + tempMax.toString() + "\u00B0C"

                                binding.temperatureMinFieldColor.setBackgroundResource(findColorFromTemp(tempMin))
                                binding.temperatureMaxFieldColor.setBackgroundResource(findColorFromTemp(tempMax))

                                binding.windField.text = "Wiatr:\n" + wind?.getInt("speed").toString() + " m/s"
                                binding.pressureField.text = "Ciśnienie:\n" + main?.getInt("pressure").toString() + " hPa"
                                binding.sunRiseField.text = "Wschód słońca:\n" + simpleDate.format(sys?.getLong("sunrise")!! * 1000 - offSet + timezone!! * 1000).toString()
                                binding.sunSetField.text = "Zachód słońca:\n" + simpleDate.format(sys?.getLong("sunset")!! * 1000 - offSet + timezone!! * 1000).toString()

                                binding.cardTopTitle.isVisible = true
                                binding.nestedScroll.isVisible = true

                                val animation1 = AnimationUtils.loadAnimation(context, R.anim.anim_1)
                                binding.iconOfWeather2.startAnimation(animation1)

                                hideKeyboard()
                            })
                        }
                    }
                })
            }
        }

        val nameOfCity = bundle?.getString("cityName").toString()
        if(nameOfCity != "" && nameOfCity != "null") {
            view.findViewById<TextView>(R.id.inputNameOfCityField).text = nameOfCity
            findWeather()
        }

        val buttonFindWeather = view.findViewById<Button>(R.id.showWeather)
        buttonFindWeather.setOnClickListener {
            findWeather()
        }

        val cityNameSpace = view.findViewById<TextView>(R.id.inputNameOfCityField)

        cityNameSpace.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                findWeather()
                true
            } else {
                false
            }
        }
    }

    fun findColorFromTemp(temp: Int?): Int {
        if (temp != null) {
            if(temp < -10) {
                return R.color.low_low_temp
            } else if(temp in -10..0) {
                return R.color.low_temp
            } else if(temp in 1..9) {
                return R.color.high_low_temp
            } else if(temp in 10..15) {
                return R.color.low_medium_temp
            } else if(temp in 16..21) {
                return R.color.medium_temp
            } else if(temp in 22..26) {
                return R.color.high_medium_temp
            } else if(temp in 27..31) {
                return R.color.low_high_temp
            } else if(temp in 32..39) {
                return R.color.high_temp
            } else if(temp > 40) {
                return R.color.high_high_temp
            }
        }
        return R.color.medium_temp
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NoExtraWeatherFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NoExtraWeatherFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
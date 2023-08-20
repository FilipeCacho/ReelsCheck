package pt.ulusofona.deisi.cm2223.g21702361

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentMovieRegistrationBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import android.widget.Toast

import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope


class MovieRegistrationFragment : Fragment() {
    private var imdbId: String? = null
    private var movieTitle: String? = null
    private var posterPath: String? = null
    private lateinit var binding: FragmentMovieRegistrationBinding
    private lateinit var db: AppDatabase // Add this line
    private var userRatingValue: String? = null
    private var dateWatchedValue: String? = null

    private var selectedCinema: Cinema? = null // Store the selected cinema here

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_movie_registration,
            container,
            false
        )

        arguments?.let {
            imdbId = it.getString("imdbId")
            movieTitle = it.getString("movieTitle")
            posterPath = it.getString("posterPath")
        }

        // Bind the data to the XML using the binding object
        binding.movieTitle = movieTitle
        binding.posterPath = posterPath // note: this doesn't load the image but sets the data
        binding.imdbId = imdbId

        // Set the user rating and date watched variables based on your logic
        binding.userRating = userRatingValue
        binding.dateWatched = dateWatchedValue





        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext()) // Initialize the database

        posterPath?.let {
            Glide.with(requireContext())
                .load(it)
                .into(binding.posterImageViewRegistration)
        }

        //make text inside outlied text box date watched not clickble, else it shows a keyboard
        //this for the datapicker
        binding.DateWatchedText.isFocusable = false
        binding.DateWatchedText.isFocusableInTouchMode = false
        binding.DateWatchedText.isClickable = true

        //make text inside outlied text box date watched not clickble, else it shows a keyboard
        //this for the cinema selection
        binding.cinemaLocationEditText.isFocusable = false
        binding.cinemaLocationEditText.isFocusableInTouchMode = false
        binding.cinemaLocationEditText.isClickable = true

        // Set the user rating and date watched variables based on your logic
        binding.userRating = userRatingValue
        binding.dateWatched = dateWatchedValue



        binding.cinemaLocationEditText.setOnClickListener {
            showCinemaBottomSheet()
        }


        binding.userRatingEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { // When the focus is lost
                val userRatingStr = binding.userRatingEditText.text.toString()

                if (userRatingStr.isEmpty() || userRatingStr.toIntOrNull() ?: 0 < 1 || userRatingStr.toIntOrNull() ?: 0 > 10) {
                    binding.userRatingTextInputLayout.error =
                        "Please enter a rating between 1 and 10"
                } else {
                    binding.userRatingTextInputLayout.error =
                        null // Clear the error when the input is correct
                }
            }
        }

        // show datetimer picker
        binding.DateWatchedText.setOnClickListener {
            showDatePickerDialog()
        }




        binding.finishButton.setOnClickListener {
            val userRatingText = binding.userRatingEditText.text.toString()
            val dateWatchedText = binding.DateWatchedText.text.toString()
            val cinemaLocation = binding.cinemaLocationEditText.text.toString()
            val comments = binding.commentsEditText.text.toString()





            if (userRatingText.isBlank() || dateWatchedText.isBlank() || cinemaLocation.isBlank() || comments.isBlank()) {
                // Show a message indicating that all fields must be filled
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val userRating = userRatingText.toFloatOrNull()
                if (userRating == null || userRating < 1 || userRating > 10) {
                    binding.userRatingTextInputLayout.error = "Please enter a rating between 1 and 10"
                    return@setOnClickListener
                } else {
                    binding.userRatingTextInputLayout.error = null
                }

                // No need to parse the dateWatchedText into an Int. Just save it as a String.
                if (dateWatchedText.isBlank()) {
                    binding.DateWatchedText.error = "Invalid date provided."
                    return@setOnClickListener
                }

                val watchDate = System.currentTimeMillis() // Use this or convert dateWatchedText to millis depending on your requirement

                // Retrieve additional cinema information from the selected cinema
                val latitude = selectedCinema?.latitude ?: 0.0
                val longitude = selectedCinema?.longitude ?: 0.0
                val cinemaName = selectedCinema?.cinema_name ?: ""
                val address = selectedCinema?.address ?: ""
                val county = selectedCinema?.county ?: ""

                // Create a UserMovieDetails object with all the information
                val userMovieDetails = UserMovieDetails(
                    id = 0,
                    imdbId = imdbId!!, // Handle potential null values for imdbId too!
                    userRating = userRating,
                    timesWatched = dateWatchedText,
                    cinemaLocation = cinemaLocation,
                    watchDate = watchDate,
                    comments = comments,
                    latitude = latitude,
                    longitude = longitude,
                    cinemaName = cinemaName,
                    address = address,
                    county = county
                )

                // Insert user movie details into the database
                val userMovieDetailsDao: UserMovieDetailsDao = db.userMovieDetailsDao()
                viewLifecycleOwner.lifecycleScope.launch {
                    userMovieDetailsDao.insert(userMovieDetails)
                }


                // Log the information being saved
                Log.d("MovieRegistrationFragment", "Saving user movie details:")
                Log.d("MovieRegistrationFragment", "IMDb ID: $imdbId")
                Log.d("MovieRegistrationFragment", "User Rating: $userRating")
                Log.d("MovieRegistrationFragment", "Date Watched: $dateWatchedText")
                Log.d("MovieRegistrationFragment", "Cinema Location: $cinemaLocation")
                //Log.d("MovieRegistrationFragment", "Watch Date: $watchDate") dont need this one
                Log.d("MovieRegistrationFragment", "Comments: $comments")
                Log.d("MovieRegistrationFragment", "Latitude: $latitude")
                Log.d("MovieRegistrationFragment", "Longitude: $longitude")
                Log.d("MovieRegistrationFragment", "Cinema Name: $cinemaName")
                Log.d("MovieRegistrationFragment", "Address: $address")
                Log.d("MovieRegistrationFragment", "County: $county")

                // Navigate back
                findNavController().popBackStack()
            }
        }



    }


    fun showCinemaBottomSheet() {
        lifecycleScope.launch {
            val cinemas = CinemaJSON.readCinemasFromAssets(requireContext())

            // Create and show BottomSheetDialog
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val cinemaNames =
                cinemas.map { "${it.cinema_name}, ${it.address}, ${it.county}" }.toTypedArray()

            val listView = ListView(requireContext())
            listView.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, cinemaNames)
            listView.setOnItemClickListener { _, _, position, _ ->
                selectedCinema = cinemas[position] // Store the selected cinema
                binding.cinemaLocationEditText.setText("${selectedCinema?.cinema_name}, ${selectedCinema?.address}, ${selectedCinema?.county}")
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.setContentView(listView)
            bottomSheetDialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedCinema = null // Clear the selected cinema when the view is destroyed
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            // Apply the custom header color here
            R.style.CustomDatePickerDialogTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate =
                    String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.DateWatchedText.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate =
            calendar.timeInMillis  // This restricts the date picker to only allow past dates
        datePickerDialog.show()
    }
}

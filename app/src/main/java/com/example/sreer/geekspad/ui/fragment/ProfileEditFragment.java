package com.example.sreer.geekspad.ui.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.activity.DatePickActivity;
import com.example.sreer.geekspad.ui.activity.SkillsActivity;
import com.example.sreer.geekspad.utils.AppUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ProfileEditFragment extends Fragment {
  private Button mAddSkills;
  private EditText mFirstName;
  private EditText mLastName;
  private EditText mEmail;
  private EditText mPhone;
  private EditText mPassword;
  private EditText mBirthDay;
  private Spinner mCountry;
  private Spinner mState;
  private EditText mCity;
  private TextView mCalendarLink;
  private TextInputLayout mPasswordText;
  private FirebaseAuth mAuth;
  private String country;
  private String state;
  private ProgressDialog progress;
  private DatabaseReference mUserRefDatabase;
  private static final int SKILL_EDIT = 1;
  private static final int BIRTHDAY_PICK = 2;
  private User loadedUser;
  private List<String> countries =
      new ArrayList<String>() {
        {
          add("Select Country(None)");
        }
      };
  private List<String> states =
      new ArrayList<String>() {
        {
          add("Select State(None)");
        }
      };

  public ProfileEditFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.activity_signup, container, false);
    mUserRefDatabase =
        com.example.sreer.geekspad.db.Backend.database().getReference().child("users");
    mAuth = FirebaseAuth.getInstance();
    progress = new ProgressDialog(getActivity());
    mAddSkills = (Button) view.findViewById(R.id.btn_add_skills);
    mFirstName = (EditText) view.findViewById(R.id.fname);
    mLastName = (EditText) view.findViewById(R.id.lname);
    mPassword = (EditText) view.findViewById(R.id.input_password);
    mPasswordText = (TextInputLayout) view.findViewById(R.id.TextInputLayout_password);
    mBirthDay = (EditText) view.findViewById(R.id.input_year);
    mCountry = (Spinner) view.findViewById(R.id.input_country);
    mState = (Spinner) view.findViewById(R.id.input_state);
    mEmail = (EditText) view.findViewById(R.id.input_email);
    mPhone = (EditText) view.findViewById(R.id.input_phone);
    mCity = (EditText) view.findViewById(R.id.input_city);
    mCalendarLink = (TextView) view.findViewById(R.id.link_calendar);
    mPasswordText.removeAllViews();
    mPasswordText.setVisibility(View.GONE);
    mEmail.setTag(mEmail.getKeyListener());
    mEmail.setKeyListener(null);
    mEmail.setEnabled(false);
    // mAddSkills.setVisibility(View.INVISIBLE);
    mAddSkills.setText("Edit Skills");
    mAddSkills.setEnabled(false);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getActivity().setTitle("Edit Profile");
    populateCountryData();
    progress.setMessage("Loading...");
    progress.show();
    Bundle data = getActivity().getIntent().getExtras();
    if (data != null) {
      User editedUserInfo = (User) data.getParcelable("user");
      if (editedUserInfo != null) restoreValues(editedUserInfo);
      else getUserInfo();
    } else getUserInfo();
    mCalendarLink.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            setBirthday();
          }
        });
    mAddSkills.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            User user = createUser();
            goToSkills(user);
          }
        });
  }

  public void setupSpinners() {
    mCountry.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            country = (String) adapterView.getAdapter().getItem(position);
            populateStateDetails();
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
          }
        });

    mState.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            state = (String) adapterView.getAdapter().getItem(position);
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
          }
        });
  }

  public void setBirthday() {
    Intent datePicker = new Intent(getActivity(), DatePickActivity.class);
    startActivityForResult(datePicker, BIRTHDAY_PICK);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) return;
    if (requestCode == BIRTHDAY_PICK && data != null) {
      mBirthDay.setText(data.getStringExtra("date"));
    } else if (requestCode == SKILL_EDIT) {
      requireActivity().setResult(RESULT_OK, requireActivity().getIntent());
      requireActivity().finish();
    }
  }

  public void populateCountryData() {

    ArrayAdapter countryAdapter =
        ArrayAdapter.createFromResource(getActivity(), R.array.countries, R.layout.spinner_item);
    countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mCountry.setAdapter(countryAdapter);
  }

  public void populateStateDetails() {
    states.clear();
    states.add("Select State(None)");
    states.addAll(AppUtil.getStates(getActivity(), country));
    ArrayAdapter<String> statesAdapter =
        new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, states);
    statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mState.setAdapter(statesAdapter);
    if (state != null) {
      mState.setSelection(getIndex(mState, state));
      state = null;
    }
  }

  public boolean isValidEmail() {
    String email = mEmail.getText().toString().trim();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }

  public boolean isValidCity() {
    return !mCity.getText().toString().trim().isEmpty();
  }

  public boolean isEmptyFirstName() {
    return this.mFirstName.getText().toString().trim().isEmpty();
  }

  public boolean isEmptyLastName() {
    return this.mLastName.getText().toString().trim().isEmpty();
  }

  public boolean isUserEmailExist() {
    return true;
  }

  public boolean isValidPassword() {
    return mPassword.getText().toString().length() >= 6;
  }

  public boolean isValidBirthday() {
    return true;
  }

  public boolean isValidCountry() {
    return mCountry.getSelectedItemPosition() > 0;
  }

  public boolean isValidState() {
    return mState.getSelectedItemPosition() > 0;
  }

  public User createUser() {
    User user = null;
    if (isEmptyFirstName()) mFirstName.setError("FirstName is required!");
    else if (isEmptyLastName()) mLastName.setError("LastName is required!");
    else if (!isValidEmail()) mEmail.setError("Must enter a valid email");
    else if (!isValidBirthday()) mBirthDay.setError("Must enter valid date");
    else if (!isValidCountry()) {
      TextView errorText = (TextView) mCountry.getSelectedView();
      errorText.setError("Country is Required");
      errorText.setTextColor(Color.RED);
    } else if (!isValidState()) {
      TextView errorText = (TextView) mState.getSelectedView();
      errorText.setError("State is Required");
      errorText.setTextColor(Color.RED);
    } else if (!isValidCity()) {
      mCity.setError("Must enter a valid city name");
    } else {

      mFirstName.setError(null);
      mLastName.setError(null);
      mEmail.setError(null);
      mPassword.setError(null);
      mBirthDay.setError(null);

      user =
          new User(
              mFirstName.getText().toString(),
              mEmail.getText().toString(),
              mCountry.getSelectedItem().toString(),
              mState.getSelectedItem().toString());
      if (mLastName.getText().toString().length() > 0)
        user.setLastname(mLastName.getText().toString());

      if (mPhone.getText().toString().length() > 0) user.setPhone(mPhone.getText().toString());

      if (mBirthDay.getText().toString().length() > 0)
        user.setBirthDate(mBirthDay.getText().toString());

      if (mCity.getText().toString().length() > 0) user.setCity(mCity.getText().toString().trim());
      if (loadedUser != null) {
        user.setUid(loadedUser.getUid());
        user.setSkills(new java.util.LinkedHashMap<>(loadedUser.getSkills()));
        if (java.util.Objects.equals(user.getCity(), loadedUser.getCity())
            && java.util.Objects.equals(user.getState(), loadedUser.getState())
            && java.util.Objects.equals(user.getCountry(), loadedUser.getCountry())) {
          user.setLatitude(loadedUser.getLatitude());
          user.setLongitude(loadedUser.getLongitude());
        }
      }
    }

    return user;
  }

  public void goToSkills(User user) {
    if (user == null) return;
    Intent skillsView = new Intent(getActivity(), SkillsActivity.class);
    skillsView.putExtra("user", user);
    skillsView.putExtra("email", user.getEmail());
    skillsView.putExtra("ForEdit", true);
    android.content.Context context = requireContext().getApplicationContext();
    new Thread(
            () -> {
              LatLng location =
                  AppUtil.getUserLocation(
                      context,
                      user.getCity() + ", " + user.getState() + ", " + user.getCountry(),
                      1);
              if (location != null) {
                user.setLatitude(String.valueOf(location.latitude));
                user.setLongitude(String.valueOf(location.longitude));
              }
              if (isAdded())
                requireActivity()
                    .runOnUiThread(
                        () -> {
                          if (isAdded()) {
                            skillsView.putExtra("user", user);
                            startActivityForResult(skillsView, SKILL_EDIT);
                          }
                        });
            })
        .start();
  }

  public void getUserInfo() {
    User user = new User();
    if (mAuth.getCurrentUser() != null) {
      user.setEmail(mAuth.getCurrentUser().getEmail());
      com.example.sreer.geekspad.db.Backend.database()
          .getReference()
          .child("users")
          .child(mAuth.getCurrentUser().getUid())
          .addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                  if (dataSnapshot.getValue() != null) {
                    User user = FireBaseHelper.getUserFromSnapShot(dataSnapshot);
                    restoreValues(user);
                  } else {
                    progress.dismiss();
                    Toast.makeText(getActivity(), "Profile not found", Toast.LENGTH_SHORT).show();
                  }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                  Toast.makeText(getActivity(), "Loading Failed", Toast.LENGTH_SHORT).show();
                  progress.dismiss();
                }
              });
    }
  }

  private int getIndex(Spinner spinner, String myString) {
    int index = 0;

    for (int i = 0; i < spinner.getCount(); i++) {
      if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
        index = i;
        break;
      }
    }
    return index;
  }

  public void restoreValues(User user) {
    loadedUser = user;
    mAddSkills.setEnabled(true);
    mCountry.setSelection(getIndex(mCountry, user.getCountry()));
    mFirstName.setText(user.getFirstname());
    mBirthDay.setText(user.getBirthDate());
    mLastName.setText(user.getLastname());
    mEmail.setText(user.getEmail());
    mEmail.setEnabled(false);
    mPhone.setText(user.getPhone());
    country = user.getCountry();
    mCity.setText(user.getCity());
    state = user.getState();
    setupSpinners();
    progress.dismiss();
  }
}

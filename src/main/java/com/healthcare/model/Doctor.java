package com.healthcare.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter 
@Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Doctor {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String fullName;
	    private String email;
	    private String mobile;
	    private String password;

	    private String registrationNumber;
	    private String council;
	    private String specialty;
	    private int experience;
	    private String degree;

	    private String clinicName;
	    private String clinicAddress;

	    private String idProofPath;
	    private String licensePath;
	    private String degreeCertPath;
	    private String photoPath;
	    private boolean approved = false; 
	    
		public boolean isApproved() {
			return approved;
		}

		public void setApproved(boolean approved) {
			this.approved = approved;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getRegistrationNumber() {
			return registrationNumber;
		}

		public void setRegistrationNumber(String registrationNumber) {
			this.registrationNumber = registrationNumber;
		}

		public String getCouncil() {
			return council;
		}

		public void setCouncil(String council) {
			this.council = council;
		}

		public String getSpecialty() {
			return specialty;
		}

		public void setSpecialty(String specialty) {
			this.specialty = specialty;
		}

		public int getExperience() {
			return experience;
		}

		public void setExperience(int experience) {
			this.experience = experience;
		}

		public String getDegree() {
			return degree;
		}

		public void setDegree(String degree) {
			this.degree = degree;
		}

		public String getClinicName() {
			return clinicName;
		}

		public void setClinicName(String clinicName) {
			this.clinicName = clinicName;
		}

		public String getClinicAddress() {
			return clinicAddress;
		}

		public void setClinicAddress(String clinicAddress) {
			this.clinicAddress = clinicAddress;
		}

		public String getIdProofPath() {
			return idProofPath;
		}

		public void setIdProofPath(String idProofPath) {
			this.idProofPath = idProofPath;
		}

		public String getLicensePath() {
			return licensePath;
		}

		public void setLicensePath(String licensePath) {
			this.licensePath = licensePath;
		}

		public String getDegreeCertPath() {
			return degreeCertPath;
		}

		public void setDegreeCertPath(String degreeCertPath) {
			this.degreeCertPath = degreeCertPath;
		}

		public String getPhotoPath() {
			return photoPath;
		}

		public void setPhotoPath(String photoPath) {
			this.photoPath = photoPath;
		}

		public Doctor(Long id, String fullName, String email, String mobile, String password, String registrationNumber,
				String council, String specialty, int experience, String degree, String clinicName,
				String clinicAddress, String idProofPath, String licensePath, String degreeCertPath, String photoPath,
				boolean approved) {
			super();
			this.id = id;
			this.fullName = fullName;
			this.email = email;
			this.mobile = mobile;
			this.password = password;
			this.registrationNumber = registrationNumber;
			this.council = council;
			this.specialty = specialty;
			this.experience = experience;
			this.degree = degree;
			this.clinicName = clinicName;
			this.clinicAddress = clinicAddress;
			this.idProofPath = idProofPath;
			this.licensePath = licensePath;
			this.degreeCertPath = degreeCertPath;
			this.photoPath = photoPath;
			this.approved = approved;
		}


		public Doctor() {
			super();
			// TODO Auto-generated constructor stub
		}

		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}

		@Override
		protected void finalize() throws Throwable {
			// TODO Auto-generated method stub
			super.finalize();
		}
		
}
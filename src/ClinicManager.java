public class ClinicManager {
    public static final String MIN_ID = "";
    public static final String MAX_ID = "\uFFFF\uFFFF\uFFFF\uFFFF";

    public TwoThreeTree doctorsTree;
    public TwoThreeTree patientsTree;
    public StatsTree loadTree;


    public ClinicManager() {
        this.doctorsTree = new TwoThreeTree();
        this.patientsTree = new TwoThreeTree();
        this.loadTree = new StatsTree();
    }


    private void validateId(String id, String type) {
        if (id.compareTo(MIN_ID) <= 0 || id.compareTo(MAX_ID) >= 0) {
            throw new IllegalArgumentException(type + " ID is out of bounds");
        }
    }


    private Doctor getDoctorOrThrow(String doctorId) {
        validateId(doctorId, "Doctor");
        Doctor doctor = (Doctor) this.doctorsTree.search(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor with this ID does not exist");
        }
        return doctor;
    }


    private void validateNewDoctor(String doctorId) {
        validateId(doctorId, "Doctor");
        if (this.doctorsTree.search(doctorId) != null) {
            throw new IllegalArgumentException("Doctor with this ID already exists");
        }
    }


    private Patient getPatientOrThrow(String patientId) {
        validateId(patientId, "Patient");
        Patient patient = (Patient) this.patientsTree.search(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Patient with this ID does not exist");
        }
        return patient;
    }


    private void validateNewPatient(String patientId) {
        validateId(patientId, "Patient");
        if (this.patientsTree.search(patientId) != null) {
            throw new IllegalArgumentException("Patient with this ID already exists");
        }
    }

    public void doctorEnter(String doctorId) {
        validateNewDoctor(doctorId);

        Doctor doctor = new Doctor(doctorId);
        this.doctorsTree.insert(doctorId, doctor);

        this.loadTree.increaseCount(0);
    }

    public void doctorLeave(String doctorId) {
        Doctor doctor = getDoctorOrThrow(doctorId);

        if (doctor.queueTree.getSize() != 0) {
            throw new IllegalArgumentException("Doctor has patients who are waiting");
        }

        this.loadTree.decreaseCount(0);
        this.doctorsTree.delete(doctorId);
    }

    public void patientEnter(String doctorId, String patientId) {
        Doctor doctor = getDoctorOrThrow(doctorId);
        validateNewPatient(patientId);

        int oldLoad = doctor.queueTree.getSize();

        Patient patient = new Patient(patientId);
        patient.setDoctor(doctor);


        doctor.queueTree.insert(patient);
        this.patientsTree.insert(patientId, patient);

        this.loadTree.decreaseCount(oldLoad);
        this.loadTree.increaseCount(oldLoad + 1);
    }

    public String nextPatientLeave(String doctorId) {
        Doctor doctor = getDoctorOrThrow(doctorId);
        if (doctor.queueTree.getSize() == 0) {
            throw new IllegalArgumentException("Doctor has no patients waiting");
        }

        int oldLoad = doctor.queueTree.getSize();

        Node nextPatientNode = doctor.queueTree.getMinimum();
        Patient nextPatient = nextPatientNode.getVal();
        String nextPatientId = nextPatient.getId();

        doctor.queueTree.delete(nextPatientNode);
        this.patientsTree.delete(nextPatientId);

        this.loadTree.decreaseCount(oldLoad);
        this.loadTree.increaseCount(oldLoad - 1);

        return nextPatientId;
    }

    public void patientLeaveEarly(String patientId) {
        Patient patient = getPatientOrThrow(patientId);
        Doctor doctor = patient.getDoctor();

        int oldLoad = doctor.queueTree.getSize();

        Node patientNode = doctor.queueTree.searchKey(patient.getTicketNumber());

        if (patientNode == null) {
            throw new IllegalArgumentException("Inconsistency: Patient not found in doctor's queue");
        }

        doctor.queueTree.delete(patientNode);
        this.patientsTree.delete(patientId);

        this.loadTree.decreaseCount(oldLoad);
        this.loadTree.increaseCount(oldLoad - 1);
    }

    public int numPatients(String doctorId) {
        Doctor doctor = getDoctorOrThrow(doctorId);
        return doctor.queueTree.getSize();
    }

    public String nextPatient(String doctorId) {
        Doctor doctor = getDoctorOrThrow(doctorId);
        if (doctor.queueTree.getSize() == 0) {
            throw new IllegalArgumentException("Doctor has no patients waiting");
        }
        Node nextPatientNode = doctor.queueTree.getMinimum();
        Patient nextPatient = nextPatientNode.getVal();
        return nextPatient.getId();
    }

    public String waitingForDoctor(String patientId) {
        Patient patient = getPatientOrThrow(patientId);
        return patient.getDoctor().getId();
    }

    public int numDoctorsWithLoadInRange(int low, int high) {
        return loadTree.getCountDoctorsLeq(high) - loadTree.getCountDoctorsLeq(low - 1);
    }

    public int averageLoadWithinRange(int low, int high) {
        int totalDoctors = numDoctorsWithLoadInRange(low, high);

        if (totalDoctors == 0) {
            return 0;
        }

        int totalPatients = this.loadTree.getSumPatientsLeq(high) - this.loadTree.getSumPatientsLeq(low - 1);

        return (int) (totalPatients / totalDoctors);
    }
}
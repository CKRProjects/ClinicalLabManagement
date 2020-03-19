import { ReportsService } from "./../services/reports.service";
import { ToastService } from "./../shared/toast-service";
import { TestsService } from "./../services/tests.service";
import { TestsListComponent } from "./../tests-list/tests-list.component";
import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-create-report",
  templateUrl: "./create-report.component.html",
  styleUrls: ["./create-report.component.css"]
})
export class CreateReportComponent implements OnInit {
  public reportForm;
  public selectedTests = [];
  public availableTests = [];
  public genders = ["male", "female", "Others"];
  public today = new Date().toDateString();
  public printTests = [];
  public counter;
  @ViewChild("loading", { read: ElementRef }) loading: ElementRef;
  constructor(
    private fb: FormBuilder,
    private modalService: NgbModal,
    private _testService: TestsService,
    private toastService: ToastService,
    private reportService: ReportsService
  ) {}

  ngOnInit(): void {
    this.counter = 0;
    this.reportForm = this.fb.group({
      patientName: ["", [Validators.required, Validators.minLength(3)]],
      age: [null, [Validators.required]],
      gender: [null, [Validators.required]],
      mobile: [
        "",
        [
          Validators.required,
          Validators.minLength(10),
          Validators.maxLength(10)
        ]
      ]
    });
    this._testService.getTests().subscribe(
      response => {
        this.loading.nativeElement.style.display = "none";
        this.availableTests = response.tests;
      },
      error => {
        this.toastService.show(error.statusText, {
          classname: "bg-danger text-light"
        });
      }
    );
  }
  changeGender(e) {
    this.reportForm.get("gender").setValue(e.target.value, {
      onlySelf: true
    });
  }
  removeTest(test) {
    this.selectedTests.splice(this.selectedTests.indexOf(test.name), 1);
  }
  addTest() {
    const modalRef = this.modalService.open(TestsListComponent, {
      scrollable: true,
      centered: true,
      size: "lg"
    });
    modalRef.componentInstance.selectedTests = this.selectedTests;
    modalRef.componentInstance.availableTests = this.availableTests;
  }
  getSelectedTests() {
    return this.availableTests.filter(element => {
      return this.selectedTests.indexOf(element.name) >= 0;
    });
  }
  saveReport() {
    if (confirm("Are you sure to create a report?")) {
      this.loading.nativeElement.style.display = "block";
      this.reportService
        .createReport(this.reportForm, this.selectedTests)
        .subscribe(
          response => {
            this.loading.nativeElement.style.display = "none";
            this.toastService.show("Report generated!!", {
              classname: "bg-success text-light"
            });
            this.printTests = [...response.reportIds];
            this.counter = 0;
            this.reportForm.reset();
            this.selectedTests.splice(0, this.selectedTests.length);
          },
          error => {
            this.toastService.show(error.statusText, {
              classname: "bg-danger text-light"
            });
          }
        );
    }
  }
  getTotalAmount() {
    let total = 0;
    this.availableTests.forEach(test => {
      if (this.selectedTests.includes(test.name)) {
        total += parseInt(test.price);
      }
    });
    return !!total ? total : 0;
  }
  get patientName() {
    return this.reportForm.get("patientName");
  }
  get age() {
    return this.reportForm.get("age");
  }
  get gender() {
    return this.reportForm.get("gender");
  }
  get mobile() {
    return this.reportForm.get("mobile");
  }
  getReportId(test) {
    this.counter++;
    if (this.counter === this.printTests.length) {
      window.print();
    }
    return test;
  }
}

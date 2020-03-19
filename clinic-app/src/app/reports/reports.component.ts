import { Router, ActivatedRoute } from "@angular/router";
import { ReportsService } from "./../services/reports.service";
import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { ToastService } from "../shared/toast-service";

@Component({
  selector: "app-reports",
  templateUrl: "./reports.component.html",
  styleUrls: ["./reports.component.css"]
})
export class ReportsComponent implements OnInit {
  public reports = [];
  public displayReports = [];
  @ViewChild("loading", { read: ElementRef }) loading: ElementRef;
  constructor(
    private _reportsService: ReportsService,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this._reportsService.getReports().subscribe(
      response => {
        this.loading.nativeElement.style.display = "none";
        this.reports = response.reports;
        this.displayReports = response.reports;
      },
      error => {
        this.toastService.show(error.statusText, {
          classname: "bg-danger text-light"
        });
      }
    );
  }
  getReport(id) {
    this.router.navigate(["/report", id]);
  }
  filterReports(event) {
    if (event.target.value.length > 0)
      this.displayReports = this.reports.filter(value => {
        return (
          value.patientName.includes(event.target.value) ||
          value.reportId.includes(event.target.value)
        );
      });
    else {
      this.displayReports = [...this.reports];
    }
  }
}

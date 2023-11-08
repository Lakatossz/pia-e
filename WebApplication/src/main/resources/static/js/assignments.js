class AssignmentsController {

    #btn_group;
    #btn_ungroup;
    #btn_group_by_project;

    #element_group;
    #element_ungroup;
    #element_group_by_project;

    constructor(btn_group, btn_ungroup, btn_group_by_project,
                element_group, element_ungroup, element_group_by_project) {
        this.#btn_group = btn_group;
        this.#btn_ungroup = btn_ungroup;
        this.#btn_group_by_project = btn_group_by_project;
        this.#element_group = element_group;
        this.#element_ungroup = element_ungroup;
        this.#element_group_by_project = element_group_by_project;

        this.show_grouped_assignments();
    }

    show_grouped_assignments() {
        document.getElementById(this.#btn_group).disabled = true;
        document.getElementById(this.#btn_ungroup).disabled = false;
        document.getElementById(this.#btn_group_by_project).disabled = false;

        document.getElementById(this.#element_group).hidden = false;
        document.getElementById(this.#element_ungroup).hidden = true;
        document.getElementById(this.#element_group_by_project).hidden = true;
    }

    show_ungrouped_assignments() {
        document.getElementById(this.#btn_group).disabled = false;
        document.getElementById(this.#btn_ungroup).disabled = true;
        document.getElementById(this.#btn_group_by_project).disabled = false;

        document.getElementById(this.#element_group).hidden = true;
        document.getElementById(this.#element_ungroup).hidden = false;
        document.getElementById(this.#element_group_by_project).hidden = true;
    }

    show_grouped_by_project_assignments() {
        document.getElementById(this.#btn_group).disabled = false;
        document.getElementById(this.#btn_ungroup).disabled = false;
        document.getElementById(this.#btn_group_by_project).disabled = true;

        document.getElementById(this.#element_group).hidden = true;
        document.getElementById(this.#element_ungroup).hidden = true;
        document.getElementById(this.#element_group_by_project).hidden = false;
    }
}
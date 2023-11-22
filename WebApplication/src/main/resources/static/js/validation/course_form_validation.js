function CourseRestriction(name) {
    this.name = name;
}

/**
 * Validates courses integrity
 */
class CourseValidation {

    #restrictions = [];

    #text;
    #manager;
    #employees = [];

    /**
     * @param restrictions restrictions data
     * @param input_abbrevation input abbrevation id
     * @param input_name input name id
     */
    constructor(restrictions = [], input_text, employees = [], input_manager) {
        this.#restrictions = restrictions;

        this.#text = document.getElementById(input_text);

        this.#employees = employees;
        this.#manager = document.getElementById(input_manager);

        this.#setupValidation();
    }

    #setupValidation() {

    }

    exclude() {

    }
}
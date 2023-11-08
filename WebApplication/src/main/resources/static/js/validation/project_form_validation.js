function ProjectRestrictions(name) {
    this.name = name;
}

/**
 * Validats projects integrity
 */
class ProjectValidation {

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
        let content = this;

        let pattern = '^(?!(';
        restrictions.forEach((n) => {
            pattern += '(' + n.name + ')' + '|';
        });
        pattern += '(\\s*)';
        pattern += ')$).*$';
        content.#text.setAttribute('pattern', pattern);

        if (content.#manager.value === '') {
            content.#manager.value = '...';
        }
        let pattern2 = ''
        content.#employees.forEach((n) => {
            pattern2 += '(' + n.replace('(', '\\(').replace(')', '\\)') + ')|';
        });
        pattern2 = pattern2.substring(0, pattern2.length - 1);
        content.#manager.setAttribute('pattern', pattern2);
    }

    exclude() {
        let nam = this.#text.value;
        let index;
        this.#restrictions.forEach((n) => {
            if (n.name === nam)
                index = this.#restrictions.indexOf(n);
        });
        this.#restrictions.splice(index, 1);
        this.#setupValidation();
    }
}
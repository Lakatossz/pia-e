/**
 * Contains restricted items for workplace form.
 *
 * @param abbrevation workplace abbrevation
 * @param name workplace name
 * @constructor creates new restriction item
 */
function WorkplaceRestrictions(abbrevation, name) {
    this.abbrevation = abbrevation;
    this.name = name;
}

/**
 * Sets custom pattern for workplace validation.
 */
class WorkplaceValidation {

    #restrictions = [];

    #input_abbrevation;
    #input_name;

    /**
     * @param restrictions restrictions data
     * @param input_abbrevation input abbrevation id
     * @param input_name input name id
     */
    constructor(restrictions = [], input_abbrevation, input_name) {
        this.#restrictions = restrictions;

        this.#input_abbrevation = document.getElementById(input_abbrevation);
        this.#input_name = document.getElementById(input_name);

        this.#setupValidation();
    }

    #setupValidation() {
        let content = this;

        this.#input_abbrevation.addEventListener('keyup', function () {
            let pattern = '^(?!(';
            restrictions.forEach((n) => {
                pattern += n.abbrevation + '|';
            });
            pattern += ')$).*$';
            content.#input_abbrevation.setAttribute('pattern', pattern);
        });
        this.#input_name.addEventListener('keyup', function () {
            let pattern = '^(?!(';
            restrictions.forEach((n) => {
                pattern += n.name + '|';
            });
            pattern += ')$).*$';
            content.#input_name.setAttribute('pattern', pattern);
        });
    }

    exclude() {
        let abb = this.#input_abbrevation.value;
        let nam = this.#input_name.value;
        let index;
        this.#restrictions.forEach((n) => {
            if (n.name === nam && n.abbrevation === abb)
                index = this.#restrictions.indexOf(n);
        });
        this.#restrictions.splice(index, 1);
    }
}
/**
 * Contains restricted items for workplace form.
 *
 * @param abbrevation workplace abbrevation
 * @param name workplace name
 * @constructor creates new restriction item
 */
function EmployeeRestrictions(orionLogin, email) {
    this.orionLogin = orionLogin;
    this.email = email;
}

/**
 * Sets custom pattern for workplace validation.
 */
class EmployeeValidation {

    #restrictions = [];

    #input_orion_login;
    #input_email;

    /**
     * @param restrictions restrictions data
     * @param input_abbrevation input abbrevation id
     * @param input_name input name id
     */
    constructor(restrictions = [], input_orion_login, input_email) {
        this.#restrictions = restrictions;

        this.#input_orion_login = document.getElementById(input_orion_login);
        this.#input_email = document.getElementById(input_email);

        this.#setupValidation();
    }

    #setupValidation() {
        let content = this;

        this.#input_orion_login.addEventListener('keyup', function () {
            let pattern = '^(?!(';
            restrictions.forEach((n) => {
                pattern += n.orionLogin + '|';
            });
            pattern += ')$).*$';
            content.#input_orion_login.setAttribute('pattern', pattern);
        });
        this.#input_email.addEventListener('keyup', function () {
            let pattern = '^(?!(';
            restrictions.forEach((n) => {
                pattern += n.email + '|';
            });
            pattern += ')$).*$';
            content.#input_email.setAttribute('pattern', pattern);
        });
    }

    exclude() {
        let oLogin = this.#input_orion_login.value;
        let email = this.#input_email.value;
        let index;
        this.#restrictions.forEach((n) => {
            if (n.orionLogin === oLogin && n.email === email)
                index = this.#restrictions.indexOf(n);
        });
        this.#restrictions.splice(index, 1);
    }
}

/**
 * Single item restrictions
 */
function HierarchyRestrictions(text) {
    this.orionLogin = text;
}

/**
 * Hirerarchy validations
 */
class HierarchyValidation {

    #restrictions = [];

    #text

    /**
     * @param restrictions restrictions data
     * @param input_abbrevation input abbrevation id
     * @param input_name input name id
     */
    constructor(restrictions = [], input_text) {
        this.#restrictions = restrictions;

        this.#text = document.getElementById(input_text);

        this.#setupValidation();
    }

    #setupValidation() {
        let content = this;

        let pattern = '^(?!((...)|';
        restrictions.forEach((n) => {
            pattern += '(' + n.orionLogin + ')|';
        });
        pattern += '(\\s*)';
        pattern += ')$).*$';
        content.#text.setAttribute('pattern', pattern);
    }
}
/**
 * Defines a variable which is used for searching employees.
 *
 * @param id
 * @param searchText
 * @constructor
 */
function Employee_criteria(id, searchText) {
    this.id = id;
    this.searchText = searchText;
}

/**
 * Filter for searching employees
 */
class EmployeeFilter {

    #criteria = [];
    #input_element;

    constructor(employee_criteria, input_element) {
        this.#criteria = employee_criteria;
        this.#input_element = input_element;

        this.#setupFilter();
    }

    #setupFilter() {
        let content = this;

        let input = $('#' + this.#input_element);
        input.on('keyup', function () {
            content.#criteria.forEach(value => {
                let employee = value.id;

                if (value.searchText.toLowerCase().includes(input.val().toLowerCase())) {
                    $('#' + employee).show();
                } else {
                    $('#' + employee).hide();
                }
            });
        });
    }
}


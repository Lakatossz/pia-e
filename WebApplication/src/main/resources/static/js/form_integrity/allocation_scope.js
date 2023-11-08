/**
 * Controller for integrity check in form input for scope. Allows input both in x/FTE or hours/week and
 * dynamically changes the other input value to correct value.
 */
class AllocationScopeController {
    scope_fte;
    scope_hpw;

    fte_min = 0.0;
    fte_max = 1.0;
    hpw_min = 0.0;
    hpw_max = 40.0;

    fte_step = 0.025;
    hpw_step = 1;

    /**
     * @param scope_fte id of scope input in x/FTE format
     * @param scope_hpw id of scope input in hours/week format
     */
    constructor(scope_fte, scope_hpw) {
        this.scope_fte = '#' + scope_fte;
        this.scope_hpw = '#' + scope_hpw;

        this.#setupInputs();
        this.#setupControllerEvents();
    }

    #setupInputs() {
        $(this.scope_fte).val(0.250);
        $(this.scope_hpw).val(10);

        $(this.scope_fte).attr('step', this.fte_step);
        $(this.scope_hpw).attr('step', this.hpw_step);

        this.setupConstraints();
    }

    #setupControllerEvents() {
        let content = this;
        $(this.scope_fte).on('change keyup', function () {
            content.changeHPW();
        });
        $(this.scope_hpw).on('change keyup', function () {
            content.changeFTE();
        });
    }

    /**
     * Changes hours/week input field value by x/FTE input field.
     */
    changeHPW() {
        let newVal = $(this.scope_fte).val() * 40.0;
        let integrityVal = Math.min(Math.max(newVal, this.hpw_min), this.hpw_max);
        if (newVal !== integrityVal)
            this.changeFTE();
        $(this.scope_hpw).val(integrityVal);
    }

    /**
     * Changes x/FTE input field value by hours/week input field.
     */
    changeFTE() {
        let newVal = $(this.scope_hpw).val() / 40.0;
        let integrityVal = Math.min(Math.max(newVal, this.fte_min), this.fte_max);
        if (newVal !== integrityVal)
            this.changeHPW();
        $(this.scope_fte).val(integrityVal);
    }

    /**
     * Sets scope constraints.
     *
     * @param new_fte_min x/FTE min value
     * @param new_fte_max x/FTE max value
     * @param new_hpw_min hours/week min value
     * @param new_hpw_max hours/week max value
     */
    setupConstraints(new_fte_min = this.fte_min, new_fte_max = this.fte_max,
                     new_hpw_min = this.hpw_min, new_hpw_max = this.hpw_max) {
        this.fte_min = new_fte_min;
        this.fte_max = new_fte_max;
        this.hpw_min = new_fte_min;
        this.hpw_max = new_hpw_max;

        $(this.scope_fte).attr('min', this.fte_min);
        $(this.scope_fte).attr('max', this.fte_max);
        $(this.scope_hpw).attr('min', this.hpw_min);
        $(this.scope_hpw).attr('max', this.hpw_max);
    }

    /**
     * Forces bot fields value integrity check by x/FTE value.
     */
    refresh() {
        this.changeFTE();
    }
}

/**
 * Controller for integrity check in form consisting of scope range made by four input fields.
 * The four input fields are:
 * - scope from in x/fte
 * - scope from in hours/week
 * - scope to in x/fte
 * - scope to in hours/week
 */
class AllocationScopeFilterController {
    #scope_from;
    #scope_to;

    /**
     * Creates new integrity check for specific input fields.
     *
     * @param scope_from_fte scope from in x/FTE
     * @param scope_from_hpw scope from in hours/week
     * @param scope_to_fte scope to in x/FTE
     * @param scope_to_hpw scope to in hours/week
     */
    constructor(scope_from_fte, scope_from_hpw, scope_to_fte, scope_to_hpw) {
        this.#scope_from = new AllocationScopeController(scope_from_fte, scope_from_hpw);
        this.#scope_to = new AllocationScopeController(scope_to_fte, scope_to_hpw);

        this.#setupInputs();
        this.#setupControllerEvents();
    }

    #setupInputs() {
        let t = this.#scope_to;
        $(t.scope_fte).val(t.fte_max);
        $(t.scope_hpw).val(t.hpw_max);
    }

    #setupControllerEvents() {
        let content = this;

        let f = this.#scope_from;
        $(f.scope_fte).on('change keyup', function () {
            content.#setMinIntegrity();
        });
        $(f.scope_hpw).on('change keyup', function () {
            content.#setMinIntegrity();
        });
        let t = this.#scope_to;
        $(t.scope_fte).on('change keyup', function () {
            content.#setMaxIntegrity();
        });
        $(t.scope_hpw).on('change keyup', function () {
            content.#setMaxIntegrity();
        });
    }

    #setMaxIntegrity() {
        let f = this.#scope_from;
        let t = this.#scope_to;
        f.setupConstraints(
            f.fte_min, $(t.scope_fte).val(), f.hpw_min, $(t.scope_hpw).val()
        );
        f.refresh();
    }

    #setMinIntegrity() {
        let f = this.#scope_from;
        let t = this.#scope_to;
        t.setupConstraints(
            $(f.scope_fte).val(), t.fte_max, $(t.scope_hpw).val(), t.hpw_max
        );
        t.refresh();
    }
}
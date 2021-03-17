%Auto-generated from sbmlImportFunExternal

% This function emulates the MathML piecewise function.
function output = piecewise(varargin)

% retrieve conditions and values
inputs     = [varargin{:}];
nbCond     = floor(numel(inputs)/2);
conditions = logical(inputs(2:2:2*nbCond));
values     = inputs(1:2:2*nbCond-1);

% default value
if nbCond ~= numel(inputs)/2
    default = inputs(end);
else
    default = NaN;
end

% retrieve values for conditions that are true
values = values(conditions);

% set output to associated value or to default if no condition is true
if isempty(values)
    output = default;
else % take last true condition
    output = values(end);
end

end